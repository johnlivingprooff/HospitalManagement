package app.controllers.auth;


import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.templating.Model;
import app.core.validation.DataValidator;
import app.core.validation.ValidationStage;
import app.models.Subject;
import app.models.account.Account;
import app.models.auth.PasswordReset;
import app.models.auth.Portal;
import app.models.patient.Patient;
import app.models.permission.Permission;
import app.models.role.Role;
import app.models.role.Roles;
import app.services.audit.AuditService;
import app.services.auth.AuthService;
import app.services.messaging.MessagingService;
import app.services.patient.PatientService;
import app.services.permission.PermissionService;
import app.services.system.SystemService;
import app.services.user.AccountService;
import app.util.ElementBuilder;
import app.util.LocaleUtil;
import app.util.NetUtils;
import app.util.UrlUtils;
import spark.Request;
import spark.Response;
import spark.Session;
import spark.route.HttpMethod;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RouteController(path = "/Auth")
@SuppressWarnings({"WeakerAccess", "unused"})
public final class AuthController extends Controller {

    @Inject
    private AuthService authService;

    @Inject
    private AuditService auditService;

    @Inject
    private AccountService accountService;

    @Inject
    private PatientService patientService;

    @Action(path = "/ResetPassword", checkPermission = false)
    public String getResetForm(Request request, Response response) {
        if (isUserLoggedIn(request)) {
            setSessionErrorMessage("You cannot change your password using this method when you're logged in", request);
            temporaryRedirect("/", response);
        }

        final Map<String, Object> model = createModel();
        addSettingsToModel(model);
        return renderView("auth/reset.html", model);
    }

    private Long getSubjectId(Request request) {
        String id;

        if ((id = getQueryParameter(request, "subject-id")) != null) {
            try {
                return Long.parseLong(id);
            } catch (Exception e) {
                getLogger().error("Invalid subject id format", e);
            }
        }
        return null;
    }

    private Subject.SubjectType getSubjectTypeFromRequest(Request request) {
        Integer ordinal;
        if ((ordinal = getNumericQueryParameter(request, "subject-type-ordinal", Integer.class)) != null) {
            try {
                return Subject.SubjectType.VALUES[ordinal];
            } catch (Exception e) {
                getLogger().error("Invalid subject type in reset link", e);
            }
        }
        return null;
    }

    private String getToken(Request request) {
        String token;
        try {
            if (!LocaleUtil.isNullOrEmpty(token = getQueryParameter(request, "jwt-token"))) {
                return token;
            }
        } catch (Exception e) {
            getLogger().error("Invalid error when retrieving token from reset link", e);
        }
        return null;
    }

    @Action(path = "/ResetPassword/:subject-type-ordinal/:subject-id/:jwt-token", checkPermission = false)
    public String getPasswordChangeForm(Request request, Response response) {
        final Model model;
        final Long subjectId;
        final String token;
        final AuthService authService;
        final Subject.SubjectType subjectType;
        final Subject subject;
        final AccountService accountService;
        final PatientService patientService;

        if (isUserLoggedIn(request)) {
            setSessionErrorMessage("You cannot change your password using this method when you're logged in", request);
            temporaryRedirect("/", response);
        }

        if ((subjectId = getSubjectId(request)) == null || (subjectType = getSubjectTypeFromRequest(request)) == null || (token = getToken(request)) == null) {
            warn(request, "Bad password reset token from " + request.ip());
            return resourceNotFound(response);
        }

        authService = getService(AuthService.class);
        accountService = getService(AccountService.class);
        patientService = getService(PatientService.class);

        switch (subjectType) {
            case STA:
                subject = accountService.getAccountById(subjectId);
                break;
            case STP:
                subject = patientService.findById(subjectId);
                break;
            default:
                subject = null;
                break;
        }

        if (subject == null) {
            setSessionErrorMessage("Invalid or expired password reset link", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        if (!authService.verifyPasswordResetToken(token, subject)) {
            setSessionErrorMessage("Invalid or expired password reset link", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        model = createModel();
        model.put("token", token);
        model.put("subjectId", subjectId);
        model.put("subjectType", subjectType);
        addSettingsToModel(model);
        return renderView("auth/new-password.html", model);
    }

    @Action(path = "/ChangePassword", checkPermission = false, method = HttpMethod.post)
    public String changePassword(Request request, Response response) {
        final Model model;
        final String token;
        final long subjectId;
        final String password;
        final Subject subject;
        final AuthService authService;
        final AccountService accountService;
        final PatientService patientService;
        final Subject.SubjectType subjectType;

        if (isUserLoggedIn(request)) {
            setSessionErrorMessage("You cannot change your password using this method when you're logged in", request);
            temporaryRedirect("/", response);
        }

        if (!validatePostData(request, PasswordReset.class, ValidationStage.Update)) {
            setSessionErrorMessage("Something went wrong. Please try again or contact support", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        token = requestAttribute("token", request);
        password = requestAttribute("password", request);
        subjectId = requestAttribute("subject", request);
        subjectType = requestAttribute("type", request);

        authService = getService(AuthService.class);
        accountService = getService(AccountService.class);
        patientService = getService(PatientService.class);

        // get subject
        switch (subjectType) {
            case STP:
                subject = patientService.findById(subjectId);
                break;
            case STA:
                subject = accountService.getAccountById(subjectId);
                break;
            default:
                subject = null;
                break;
        }
        if (subject == null) {
            setSessionErrorMessage("Invalid or expired password reset link", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        if (!authService.verifyPasswordResetToken(token, subject)) {
            setSessionErrorMessage("Invalid or expired password reset link", request);
            return temporaryRedirect(getBaseUrl(), response);
        }

        if (authService.verifyPassword(password, subject.getPassword())) {
            setSessionErrorMessage("New password must be different from your old one.", request);
            model = createModel();
            copyMessagesToModel(request, model);
            model.put("token", token);
            model.put("subjectId", subjectId);
            model.put("subjectType", subjectType);
            addSettingsToModel(model);
            return renderView("auth/new-password.html", model);
        }

        authService.updateSubjectPassword(password, subject);

        sendUpdateNotificationEmail(subject, request.ip());

        warn(request, "Password changed for " + subject.toString());

        setSessionSuccessMessage("Your password was successfully updated", request);
        return temporaryRedirect(getBaseUrl(), response);
    }

    @DataValidator.Schema("auth/login")
    @Action(path = "/Login", method = HttpMethod.post, checkPermission = false)
    public String login(Request request, Response response) {
        final Portal portal;
        final String email, password;
        final Model model;

        // Run the validation filter
        if (!validatePostData(request, "login")) {
            model = createModel();
            addSettingsToModel(model);
            copyErrorListToModel(model, request);
            copyRawPostDataToModel(model, request);
            model.put("portals", Portal.PORTALS);
            return renderView("auth/login.html", model);
        }

        //
        email = requestAttribute("email", request);
        portal = requestAttribute("portal", request);
        password = requestAttribute("password", request);

        switch (portal) {
            case Staff:
                return authenticateStaffAccount(email, password, request, response);
            case Patient:
                return authenticatePatient(email, password, request, response);
            default:
                setSessionErrorMessage("Unknown login portal", request);
                return temporaryRedirect(getBaseUrl(), response);
        }
    }

    private String authenticatePatient(String email, String password, Request request, Response response) {
        final Model model;
        final Patient patient;

        patient = patientService.getPatientByEmail(email);

        if (patient == null) {
            model = createModel();
            addSettingsToModel(model);
            model.put("portals", Portal.PORTALS);
            copyRawPostDataToModel(model, request);
            model.put("errorList", new ArrayList<String>() {{
                add("Invalid email address or password");
            }});

            auditService.auth(String.format(Locale.US, "Patient authentication failed for '%s'", email), "/Auth/Login", request.ip());
            return renderView("auth/login.html", model);
        }

        if (!authService.verifyPassword(password, patient.getPassword())) {
            model = createModel();
            addSettingsToModel(model);
            copyRawPostDataToModel(model, request);
            model.put("portals", Portal.PORTALS);
            model.put("errorList", new ArrayList<String>() {{
                add("Invalid email address or password");
            }});

            // security audit trail
            auditService.auth(String.format(Locale.US,
                    "Patient authentication failed for '%s'", email), "/Auth/Login", request.ip());
            return renderView("auth/login.html", model);
        }

        // Make sure the account is active
        if (!patient.isActive()) {
            model = createModel();
            copyRawPostDataToModel(model, request);
            model.put("portals", Portal.PORTALS);
            addSettingsToModel(model);
            model.put("errorList", new ArrayList<String>() {{
                add("This account cannot login at this moment.");
            }});
            auditService.auth(String.format(Locale.US, "Patient %s attempted to login with disabled credentials",
                    patient.getAuditLogString()), "/Auth/Login", request.ip());
            return renderView("auth/login.html", model);
        }

        // Save account related data to session

        List<Permission> userPermissions = getService(PermissionService.class).getRolePermissionList(Roles.Patient);

        Role userRole = getService(PermissionService.class).getRole(Roles.Patient);
        Session session = request.session(false);

        if (session != null) {
            session.invalidate();
        }

        session = request.session(true);

        // Remove password (hash) so we don't save it to disk
        patient.setPassword(null);

        int sessionTTLSeconds = (int) TimeUnit.MINUTES.toSeconds(accountService.getSystemConfiguration().SessionTTLMinutes);

        session.attribute("account", patient);
        session.attribute("userRole", userRole);
        session.attribute("userPermissions", userPermissions);
        session.attribute("subjectType", patient.getSubjectType());
        session.maxInactiveInterval(sessionTTLSeconds);

        auditService.auth(String.format(Locale.US, "Patient %s successfully logged in",
                patient.getAuditLogString()), "/Auth/Login", request.ip());
        return temporaryRedirect("/", response);
    }

    private String authenticateStaffAccount(String email, String password, Request request, Response response) {
        final Model model;

        // find account
        final AccountService accountService = getService(AccountService.class);
        final AuthService authService = getService(AuthService.class);

        final Account account = accountService.getAccountByEmail(email);

        if (account == null) {
            model = createModel();
            addSettingsToModel(model);
            model.put("portals", Portal.PORTALS);
            copyRawPostDataToModel(model, request);
            model.put("errorList", new ArrayList<String>() {{
                add("Invalid email address or password");
            }});

            // security audit trail
            auditService.auth(String.format(Locale.US, "Authentication failed for '%s'", email), "/Auth/Login", request.ip());

            return renderView("auth/login.html", model);
        }

        if (!authService.verifyPassword(password, account.getPassword())) {
            model = createModel();
            addSettingsToModel(model);
            copyRawPostDataToModel(model, request);
            model.put("portals", Portal.PORTALS);
            model.put("errorList", new ArrayList<String>() {{
                add("Invalid email address or password");
            }});

            // security audit trail
            auditService.auth(String.format(Locale.US,
                    "Authentication failed for '%s'", email), "/Auth/Login", request.ip());

            return renderView("auth/login.html", model);
        }

        // Make sure the account is active
        if (!account.isActive()) {
            model = createModel();
            copyRawPostDataToModel(model, request);
            model.put("portals", Portal.PORTALS);
            addSettingsToModel(model);
            model.put("errorList", new ArrayList<String>() {{
                add("This account has been disabled. Please contact your system administrator");
            }});

            // security audit trail
            auditService.auth(String.format(Locale.US, "%s attempted to login with disabled credentials",
                    account.getAuditLogString()), "/Auth/Login", request.ip());

            return renderView("auth/login.html", model);
        }

        // Save account related data to session

        List<Permission> userPermissions = getService(PermissionService.class).getRolePermissionList(account.getRoleKey());
        userPermissions.addAll(getService(PermissionService.class).getAccountTypePermissions(account.getAccountType()));

        Role userRole = getService(PermissionService.class).getRole(account.getRoleKey());
        Session session = request.session(false);

        if (userRole.isAdministrator()) {
            if (getService(SystemService.class).getSystemConfiguration().LocalHostLogInOnly) {
                if (!NetUtils.isLoopbackAddress(request.ip())) {
                    model = createModel();
                    copyRawPostDataToModel(model, request);
                    addSettingsToModel(model);
                    model.put("portals", Portal.PORTALS);
                    model.put("errorList", new ArrayList<String>() {{
                        add("Remote log in not allowed");
                    }});

                    auditService.auth(String.format(Locale.US, "%s attempted to login remotely but was blocked",
                            account.getAuditLogString()), "/Auth/Login", request.ip());

                    return renderView("auth/login.html", model);
                }
            }
        }

        if (!userRole.isActive()) {
            model = createModel();
            copyRawPostDataToModel(model, request);
            addSettingsToModel(model);
            model.put("portals", Portal.PORTALS);
            model.put("errorList", new ArrayList<String>() {{
                add("This account is not permitted to login");
            }});

            auditService.log(String.format(Locale.US, "%s attempted to login with a deactivated role",
                    account.getAuditLogString()), request);

            return renderView("auth/login.html", model);
        }

        if (session != null) {
            session.invalidate();
        }

        session = request.session(true);

        // Remove password (hash) so we don't save it to disk
        account.setPassword(null);

        int sessionTTLSeconds = (int) TimeUnit.MINUTES.toSeconds(accountService.getSystemConfiguration().SessionTTLMinutes);

        session.attribute("account", account);
        session.attribute("userRole", userRole);
        session.attribute("subjectType", account.getSubjectType());
        session.attribute("userPermissions", userPermissions);
        session.maxInactiveInterval(sessionTTLSeconds);

        auditService.auth(String.format(Locale.US, "%s successfully logged in",
                account.getAuditLogString()), "/Auth/Login", request.ip());
        return temporaryRedirect("/", response);
    }

    @Action(path = "/Logout", checkPermission = false)
    public String logout(Request request, Response response) {
        final Session session = request.session(false);
        if (session != null) {
            Subject account = getCurrentSubject(request);
            if (null != account) {
                getService(AuditService.class)
                        .auth(account.getAuditLogString() + " logged out", "/Auth/Logout", request.ip());
            }
            session.invalidate();
        }
        return temporaryRedirect("/", response);
    }

    @Action(path = "/", checkPermission = false)
    public String getLoginForm(Request request, Response response) {
        if (isUserLoggedIn(request)) {
            return temporaryRedirect("/", response);
        }

        final Map<String, Object> model = createModel();
        addSettingsToModel(model);
        copyMessagesToModel(request.session(false), model);
        model.put("portals", Portal.PORTALS);
        return renderView("auth/login.html", model);
    }

    private void addSettingsToModel(Map<String, Object> model) {
        SystemService systemService = getService(SystemService.class);
        model.put("systemSettings", systemService.getSettings());
        model.put("applicationVersion", systemService.getVersion());
    }

    @Action(path = "/GetResetLink", checkPermission = false, method = HttpMethod.post)
    public String getPasswordResetLink(Request request, Response response) {
        final Role role;
        Patient patient;
        final Model model;
        final String loginId;
        final Account account;
        final AccountService accountService;
        final PatientService patientService;
        final PermissionService permissionService;

        if (isUserLoggedIn(request)) {
            setSessionErrorMessage("You cannot change your password using this method when you're logged in.", request);
            temporaryRedirect("/", response);
        }

        if (!validatePostData(request, PasswordReset.class, ValidationStage.Create)) {
            warn(request, "Invalid post data for password reset");
            model = createModel();
            addSettingsToModel(model);
            return renderView("auth/reset.html", model);
        }

        loginId = requestAttribute("loginId", request);
        accountService = getService(AccountService.class);
        patientService = getService(PatientService.class);
        permissionService = getService(PermissionService.class);

        if ((account = accountService.getAccountByEmail(loginId)) != null) {
            if (!account.isActive()) {
                warn(request, "Attempt to reset password with disabled account " + account.getAuditLogString());
                sendResetNotificationEmail(
                        account,
                        "Unfortunately, this account's password cannot be reset because it has been disabled.",
                        request.ip()
                );
            } else {
                role = permissionService.getRoleById(account.getRoleId());

                if (role.isAdministrator() && accountService.getSystemConfiguration().LocalHostLogInOnly && !NetUtils.isLoopbackAddress(request.ip())) {
                    sendResetNotificationEmail(account,
                            "Unfortunately, this account's password cannot be reset because the account cannot be accessed remotely.",
                            request.ip()
                    );
                    warn(request, "Attempt to remotely reset account with same origin authentication policy on. " + account);
                } else {
                    sendPasswordResetToken(account, request.ip());
                }
            }
        } else if (((patient = patientService.getPatientByPatientId(loginId)) != null) || (patient = patientService.getPatientByEmail(loginId)) != null) {
            if (!LocaleUtil.isNullOrEmpty(patient.getEmail())) {
                if (!patient.isActive()) {
                    sendResetNotificationEmail(
                            patient,
                            "Unfortunately, this account's password cannot be reset because it has been disabled.",
                            request.ip()
                    );
                    warn(request, "Attempt to reset password for disabled patient account "
                            + patient.getAuditLogString());
                } else {
                    sendPasswordResetToken(patient, request.ip());
                }
            } else {
                warn(request, "Recovery email not sent to patient account "
                        + patient.getAuditLogString() + " because it does not have an email address");
            }
        }
        setSessionSuccessMessage("Recovery instructions have been sent to your email address", request);
        return temporaryRedirect(getBaseUrl(), response);
    }

    private void sendPasswordResetToken(Subject subject, String originatingIpAddress) {
        final Date date;
        final String token;
        final String siteUrl;
        final String message;
        final String resetLink;
        final AuthService authService;
        final MessagingService messagingService;

        authService = getService(AuthService.class);
        messagingService = getService(MessagingService.class);

        date = new Date();
        token = authService.createPasswordResetToken(subject);
        siteUrl = getService(SystemService.class).getWebsiteBaseUrl();
        resetLink = UrlUtils.make(
                siteUrl,
                "Auth/ResetPassword",
                Integer.toString(subject.getSubjectType().ordinal()),
                Long.toString(subject.getId()),
                token
        );

        // Create HTML email
        message = new ElementBuilder("div")
                .addChild(
                        new ElementBuilder("p")
                                .text("Dear " + subject.getFullName() + ",")
                ).addChild(
                        new ElementBuilder("p")
                                .text("You or someone pretending to be you requested for a password reset link on ")
                                .addChild(new ElementBuilder("strong").text(LocaleUtil.formatDate(date, true)))
                ).addChild(
                        new ElementBuilder("p")
                                .text("If you did not request for this password reset link, ignore this email address.")
                ).addChild(
                        new ElementBuilder("p")
                                .text(" to reset your password or copy and paste the link in your browser and " +
                                        "follow the instructions. Please note that the " +
                                        "link will expire in " + AuthService.RESET_TOKEN_TTL_MINUTES + " minutes", false)
                                .addChild(new ElementBuilder.Anchor().href(resetLink).text("Click here"))
                )
                .addChild(
                        new ElementBuilder("p")
                                .addChild(
                                        new ElementBuilder("h5")
                                                .text("For your information, this request originated from the following ip address ")
                                                .addChild(new ElementBuilder("strong").text(originatingIpAddress))
                                )
                ).build();

        messagingService.sendEmail(subject.getEmail(), subject.getFullName(), "Password Reset Instructions", message, true);

        getService(AuditService.class).log(
                AuditService.LogType.General,
                "Password reset token for " + subject.toString() + " sent to " + subject.getEmail(),
                "/Auth/ResetLink",
                new Date(),
                originatingIpAddress
        );
    }

    private void sendUpdateNotificationEmail(Subject subject, String ip) {
        final Date date;
        final String message;
        final MessagingService messagingService;

        messagingService = getService(MessagingService.class);

        date = new Date();

        message = new ElementBuilder("div")
                .addChild(
                        new ElementBuilder("p")
                                .text("Dear " + subject.getFullName() + ",")
                ).addChild(
                        new ElementBuilder("p")
                                .text("You or someone pretending to be you successfully updated your password on ")
                                .addChild(new ElementBuilder("strong").text(LocaleUtil.formatDate(date, true)))
                ).addChild(
                        new ElementBuilder("p")
                                .text("If you did not make this change " +
                                        "please change your password immediately or seek technical help.")
                ).addChild(
                        new ElementBuilder("p")
                                .addChild(
                                        new ElementBuilder("h5")
                                                .text("For your information, this request originated from the following ip address ")
                                                .addChild(new ElementBuilder("strong").text(ip))
                                )
                ).build();

        messagingService.sendEmail(
                subject.getEmail(),
                subject.getFullName(),
                "Your Password Was Reset",
                message,
                true
        );
    }

    private void sendResetNotificationEmail(Subject subject, String text, String ip) {
        final Date date;
        final String message;
        final MessagingService messagingService;

        messagingService = getService(MessagingService.class);

        date = new Date();

        message = new ElementBuilder("div")
                .addChild(
                        new ElementBuilder("p")
                                .text("Dear " + subject.getFullName() + ",")
                ).addChild(
                        new ElementBuilder("p")
                                .text("You or someone pretending to be you requested for a password reset link on ")
                                .addChild(new ElementBuilder("strong").text(LocaleUtil.formatDate(date, true)))
                ).addChild(
                        new ElementBuilder("p")
                                .text(text)
                ).addChild(
                        new ElementBuilder("p")
                                .addChild(
                                        new ElementBuilder("h5")
                                                .text("For your information, this request originated from the following ip address ")
                                                .addChild(new ElementBuilder("strong").text(ip))
                                )
                ).build();

        messagingService.sendEmail(
                subject.getEmail(),
                subject.getFullName(),
                "Password Reset Instructions",
                message,
                true
        );
    }

    private void warn(Request request, String message) {
        getLogger().warn(message);
        getService(AuditService.class).log(message, request);
    }
}
