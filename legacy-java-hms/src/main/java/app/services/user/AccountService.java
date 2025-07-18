package app.services.user;

import app.Configuration;
import app.core.ServiceImpl;
import app.core.annotations.ServiceDescriptor;
import app.daos.user.AccountDao;
import app.models.account.Account;
import app.models.account.Candidate;
import app.models.doctor.Doctor;
import app.models.location.Department;
import app.services.audit.AuditService;
import app.util.LocaleUtil;
import org.sql2o.Connection;

import java.io.File;
import java.util.List;

@ServiceDescriptor
public final class AccountService extends ServiceImpl {

    public AccountService(Configuration configuration) {
        super(configuration);
    }

    /**
     * <p>Add a account to the system</p>
     *
     * @param account New account to add to system
     */
    public void addAccount(Account account, AuditService.LogEntry logEntry) {
        account.setId(executeUpdate(connection -> {
            String sql = "INSERT INTO account (departmentId, email, password, firstName, lastName, dob, created, modified, " +
                    "picture, sex, roleKey, active, system, hidden, role_modifiable, account_type, phone) " +
                    "VALUES (:departmentId, :email, :password, :firstName, :lastName, :dob, :created, :modified, " +
                    ":picture, :sex, :roleKey, :active, false, false, :role_modifiable, :account_type, :phone)";
            return connection.createQuery(sql)
                    .addParameter("departmentId", account.getDepartmentId())
                    .addParameter("email", account.getEmail())
                    .addParameter("password", account.getPassword())
                    .addParameter("firstName", account.getFirstName())
                    .addParameter("lastName", account.getLastName())
                    .addParameter("dob", account.getDob())
                    .addParameter("created", account.getCreated())
                    .addParameter("modified", account.getModified())
                    .addParameter("picture", account.getPicture())
                    .addParameter("sex", account.getSex())
                    .addParameter("roleKey", account.getRoleKey())
                    .addParameter("active", account.isActive())
                    .addParameter("role_modifiable", true)
                    .addParameter("account_type", account.getAccountType())
                    .addParameter("phone", account.getPhone())
                    .executeUpdate()
                    .getKey(Long.class);
        }));

        /*openHandle().useTransaction((handle, transactionStatus) -> {
            AccountDao accountDao;

            accountDao = handle.attach(AccountDao.class);

            accountDao.addAccount(account);

//            account.setRole_modifiable(Roles.canModifyRole(account.getRoleKey()));
            account.setId(accountDao.addAccount(account));

            *//*if (Roles.Doctor.equalsIgnoreCase(account.getRoleKey())) {
                accountDao.addToDoctorsList(account);
            } else if (Roles.Nurse.equalsIgnoreCase(account.getRoleKey())) {
                accountDao.addToNursesList(account);
            }*//*
        });*/
        getService(AuditService.class).log(logEntry.getSubject() + " added account " + account, logEntry);
    }

    public Account getAccountByEmail(String email) {
        return withDao(AccountDao.class).getAccountByEmail(email);
    }

    /**
     * @return Returns a list of all active users in the system
     */
    public List<Account> getActiveAccounts() {
        return withDao(AccountDao.class).getAccounts(true);
    }

    public List<Account> getInactiveAccounts() {
        return withDao(AccountDao.class).getAccounts(false);
    }

    public String getUserPassword(long userId) {
        return withDao(AccountDao.class).getPassword(userId);
    }

    public void updateAccount(Account account) {
        updateAccount(account, null);
    }

    public void updateAccount(Account account, AuditService.LogEntry logEntry) {
        executeUpdate(connection -> {
            String sql = "update account " +
                    "set email = :email, departmentId = :departmentId, firstName = :firstName, password = :password, " +
                    "lastName = :lastName, dob = :dob, modified = :modified, picture = :picture, sex = :sex, " +
                    "roleKey = :roleKey, active = :active, account_type = :account_type, phone = :phone " +
                    "where id = :id";
            return connection.createQuery(sql)
                    .addParameter("id", account.getId())
                    .addParameter("departmentId", account.getDepartmentId())
                    .addParameter("email", account.getEmail())
                    .addParameter("password", account.getPassword())
                    .addParameter("firstName", account.getFirstName())
                    .addParameter("lastName", account.getLastName())
                    .addParameter("dob", account.getDob())
                    .addParameter("modified", account.getModified())
                    .addParameter("picture", account.getPicture())
                    .addParameter("sex", account.getSex())
                    .addParameter("roleKey", account.getRoleKey())
                    .addParameter("active", account.isActive())
                    .addParameter("account_type", account.getAccountType())
                    .addParameter("phone", account.getPhone())
                    .executeUpdate()
                    .getKey(Long.class);
        });
        if (logEntry != null) {
            getService(AuditService.class).log(logEntry.getSubject() + " updated account " + account, logEntry);
        }
    }

    public Account getUserById(long userId) {
        return withDao(AccountDao.class).getAccountById(userId);
    }

    public boolean userEmailAddressExists(String email) {
        return withDao(AccountDao.class).emailExists(email);
    }

    /**
     * <p>Returns a file object pointing to the user's profile image on disk </p>
     *
     * @param userId The user's id
     * @return File or null if the users does not have a profile image set
     */
    public File getUserProfileImage(long userId) {
        String fileName = withDao(AccountDao.class).getUserPicture(userId);
        if (LocaleUtil.isNullOrEmpty(fileName)) {
            return null;
        }
        return new File(getSystemConfiguration().ImageDirectory, fileName);
    }

    /**
     * <p>Delete the given account's profile image from disk (file) and database (name)</p>
     *
     * @param account The account whose file to delete
     * @return true if the picture existed and was deleted, or if the account did not have a picture set
     */
    @Deprecated(forRemoval = true)
    public boolean deleteUserPicture(Account account) {
        boolean deleted = false;
        if (!LocaleUtil.isNullOrEmpty(account.getPicture())) {
            try {
                final File file = new File(getSystemConfiguration().ImageDirectory, account.getPicture());
                if (deleted = file.delete()) {
                    //withDao(AccountDao.class).updateProfilePicture(account.getId(), null);
                }
            } catch (Exception e) {
                getLogger().error("Error deleting picture", e);
            }
        } else {
            deleted = true;
        }
        return deleted;
    }

    public Account getAccountById(long id) {
        return withDao(AccountDao.class).getAccountById(id);
    }

    public List<Candidate> getCandidatesByDepartment(Department department) {
        try (Connection connection = getSql2oInstance().open()) {
            return connection.createQuery("select * from getCandidatesFromDepartment(:departmentId)")
                    .addParameter("departmentId", department.getId())
                    .executeAndFetch(Candidate.class);
        } catch (Exception e) {
            throw logAndGenerateException(e.getMessage(), e);
        }
    }

    public Candidate getCandidateByDepartment(long accountId) {
        try (Connection connection = getSql2oInstance().open()) {
            return connection.createQuery("select * from getCandidateFromDepartment(:account_id)")
                    .addParameter("account_id", accountId)
                    .executeAndFetchFirst(Candidate.class);
        } catch (Exception e) {
            throw logAndGenerateException(e.getMessage(), e);
        }
    }

    public List<Account> getNurses() {
        return executeSelect(connection -> connection
                .createQuery("select * from nurses_v")
                .executeAndFetch(Account.class));
    }

    public List<Account> getDoctors() {
        return executeSelect(connection -> connection
                .createQuery("select * from doctors_v")
                .executeAndFetch(Account.class));
    }

    public Account getDoctorById(long id) {
        return executeSelect(connection -> connection
                .createQuery("select * from doctors_v where id = :id")
                .addParameter("id", id)
                .executeAndFetchFirst(Account.class));
    }

    public Account getActiveDoctorById(long id) {
        return executeSelect(connection -> connection
                .createQuery("select * from doctors_v where id = :id and active")
                .addParameter("id", id)
                .executeAndFetchFirst(Account.class));
    }

    public void updateUserPassword(long id, String password) {
        executeUpdate((SqlUpdateTask<Void>) connection -> {
            connection.createQuery("update account set password = :password where id = :id")
                    .addParameter("id", id)
                    .addParameter("password", password)
                    .executeUpdate();
            return null;
        });
    }

    public List<Doctor> getAvailableDoctors() {
        String sql = "select id, department, (firstName || ' ' || lastName) as name from doctors_v where active";
        return executeSelect(connection -> connection.createQuery(sql).executeAndFetch(Doctor.class));
    }
}