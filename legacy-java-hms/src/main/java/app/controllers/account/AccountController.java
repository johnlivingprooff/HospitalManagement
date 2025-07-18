package app.controllers.account;

import app.core.Controller;
import app.core.annotations.Action;
import app.core.annotations.Inject;
import app.core.annotations.RouteController;
import app.core.validation.DataValidator;
import app.models.Subject;
import app.models.account.Account;
import app.services.audit.AuditService;
import app.services.auth.AuthService;
import app.services.patient.PatientService;
import app.services.user.AccountService;
import spark.Request;
import spark.Response;
import spark.route.HttpMethod;

import java.io.File;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * <p>Controller for account settings. There currently is no need to have this controller guarded by permissions</p>
 */
@SuppressWarnings("WeakerAccess")
@RouteController(path = "/Hms/Account")
public final class AccountController extends Controller {

    @Inject
    private PatientService patientService;
    @Inject
    private AccountService accountService;

    @Inject
    private AuthService authService;

    //@Action(path = "/UpdateProfilePicture", method = HttpMethod.post)
    private String updateProfilePicture(Request request, Response response) {
        final Account me;
        final AuditService auditService;
        final AccountService accountService;

        me = getCurrentUser(request);
        accountService = getService(AccountService.class);
        auditService = getService(AuditService.class);

        if (handleUploadedFile("profile-image", "Profile",
                accountService.getSystemConfiguration().ImageDirectory, true, request, IMAGE_TYPES)) {

            File image = requestAttribute("profile-image", request);

            // delete old picture
            if (accountService.deleteUserPicture(me)) {
                me.setPicture(image.getName());
                setSessionObject("user", me, request);
                //accountService.updateUserPicture(me);
                setSessionSuccessMessage("Profile picture updated", request);
                auditService.user(me.getAuditLogString() + " updated their profile picture", request.pathInfo(), request.ip());
            } else {
                // delete the recently uploaded picture
                image.delete();
                setSessionErrorMessage("There was a problem updating your picture", request);
            }
        }

        return temporaryRedirect("/Hms/Account/Contact", response);
    }

    @Action(path = "/Password", checkPermission = false)
    public String getPasswordForm(Request request, Response response) {
        return renderView("account/PasswordForm.html", createModel(request));
    }

    private boolean verifyPasswords(String currentPassword, String newPassword, String hashedPassword, Request request) {
        if (!authService.verifyPassword(currentPassword, hashedPassword)) {
            setSessionErrorMessage("Current password is invalid.", request);
            return false;
        }
        if (newPassword.equals(currentPassword)) {
            setSessionErrorMessage("Your new password cannot be the same as your current.", request);
            return false;
        }
        return true;
    }

    @DataValidator.Schema("account/UpdatePassword")
    @Action(path = "/UpdatePassword", method = HttpMethod.post, checkPermission = false)
    public String updatePassword(Request request, Response response) {
        Map<String, Object> model;

        if (!validatePostData(request, "UpdatePassword")) {
            model = createModel(request);
            model.put("errorList", request.attribute("errorList"));
        } else {
            String currentPassword = request.attribute("password1");
            String newPassword = request.attribute("password2");
            String hashedPassword;

            final Subject subject = getCurrentSubject(request);

            if (subject.isPatient()) {
                hashedPassword = patientService.getPatientPassword(subject.getId());
            } else {
                hashedPassword = accountService.getUserPassword(subject.getId());
            }
            if (verifyPasswords(currentPassword, newPassword, hashedPassword, request)) {
                hashedPassword = authService.hashPassword(newPassword);
                if (subject.isPatient()) {
                    patientService.updatePatientPassword(subject.getId(), hashedPassword);
                } else {
                    accountService.updateUserPassword(subject.getId(), hashedPassword);
                }
                setSessionSuccessMessage("Your password was updated successfully", request);
            }
            model = createModel(request);
        }
        return renderView("account/PasswordForm.html", model);
    }

    //@DataValidator.Schema("UpdateContactInformation")
    //@Action(path = "/UpdateContactInformation", method = HttpMethod.post)
    public String updateContactInformation(Request request, Response response) {
        final Map<String, Object> model;
        if (!validatePostData(request, "UpdateContactInformation")) {
            /*model = createModel(request);
            model.put("firstName", postRequestParameter("firstName", request));
            model.put("lastName", postRequestParameter("lastName", request));
            model.put("address1", postRequestParameter("address1", request));
            model.put("address2", postRequestParameter("address2", request));
            model.put("phone1", postRequestParameter("phone1", request));
            model.put("phone2", postRequestParameter("phone2", request));
            model.put("errorList", request.attribute("errorList"));
            model.put("dob", request.attribute("dob"));
            model.put("email", request.attribute("email"));*/
        } else {
            final Date dob = requestAttribute("dob", request);
            final String email = requestAttribute("email", request);
            final String lastName = requestAttribute("lastName", request);
            final String firstName = requestAttribute("firstName", request);
            final String primaryAddress = requestAttribute("phone1", request);
            final String mailingAddress = requestAttribute("address2", request);
            final String physicalAddress = requestAttribute("address1", request);
            final String secondaryAddress = requestAttribute("phone2", request);

            // update address
            final Account account = getCurrentUser(request);
            final AccountService accountService = getService(AccountService.class);

            final String oldName = account.getAuditLogString();

            final boolean error;

            // Did email address change?
            if (!account.getEmail().equalsIgnoreCase(email)) {
                // Verify new email address is not already in use
                if ((error = accountService.userEmailAddressExists(email))) {
                    setSessionErrorMessage("Email address is already in use", request);
                }
            } else {
                error = false;
            }

            // Only update if everything is valid
            if (!error) {
                // also update the session object
                account.setEmail(email);
                account.setFirstName(firstName);
                account.setLastName(lastName);
                account.setDob(dob);
                account.setModified(new Date());

                request.session().attribute("account", account);

                //accountService.updateUserContact(account, physicalAddress, mailingAddress, primaryAddress, secondaryAddress);

                setSessionSuccessMessage("Personal details updated", request);

                // Add to log
                getService(AuditService.class).user(
                        String.format(
                                Locale.US,
                                "%s changed their name to %s",
                                oldName,
                                account.getAuditLogString()
                        ),
                        request.pathInfo(),
                        request.ip()
                );
            }

            /*model = createModel(request);
            model.put("address1", physicalAddress);
            model.put("address2", mailingAddress);
            model.put("phone1", primaryAddress);
            model.put("phone2", secondaryAddress);
            model.put("firstName", account.getFirstName());
            model.put("lastName", account.getLastName());
            model.put("email", account.getEmail());
            model.put("dob", account.getDateOfBirth());*/
        }
        return temporaryRedirect("/Hms/Account/Contact", response);
        //return renderView("account/ContactForm.html", model);
    }
}
