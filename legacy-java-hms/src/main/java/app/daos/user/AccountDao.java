package app.daos.user;

import app.models.account.Account;
import app.models.account.AccountMapper;
import app.models.account.ContactInfoMapper;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

@RegisterMapper({AccountMapper.class, ContactInfoMapper.class})
public interface AccountDao {

    @SqlUpdate("INSERT INTO account (departmentId, email, password, firstName, lastName, dob, created, modified, " +
            "picture, sex, roleKey, active, system, hidden, role_modifiable, account_type, phone) " +
            "VALUES (:departmentId, :email, :password, :firstName, :lastName, :dob, :created, :modified, " +
            ":picture, :sex, :roleKey, :active, false, false, :role_modifiable, :accountType, :phone)")
    @GetGeneratedKeys
    long addAccount(@BindBean Account account);

    @SqlUpdate("update account set email = :email, departmentId = :departmentId, firstName = :firstName, password = :password, " +
            "lastName = :lastName, dob = :dob, modified = :modified, picture = :picture, sex = :sex, " +
            "roleKey = :roleKey, active = :active, account_type = :account_type, phone = :phone " +
            "where id = :id")
    void updateAccount(@BindBean Account account);

    @SqlQuery("select * from accounts_v WHERE lower(email) = lower(:email) LIMIT 1")
    Account getAccountByEmail(@Bind("email") String email);

    @SqlQuery("select * from accounts_v WHERE hidden = false and active = :active ORDER BY modified DESC")
    List<Account> getAccounts(@Bind("active") boolean active);

    @SqlQuery("SELECT password FROM account WHERE Id = :id AND Active = True")
    String getPassword(@Bind("id") long userId);

    @SqlQuery("select * from accounts_v WHERE id = :id")
    Account getAccountById(@Bind("id") long accountId);

    @SqlQuery("SELECT EXISTS (SELECT id FROM account u WHERE LOWER(email) = LOWER(:email) LIMIT 1)")
    boolean emailExists(@Bind("email") String email);

    @SqlQuery("SELECT picture FROM account WHERE id = :id LIMIT 1")
    String getUserPicture(@Bind("id") long userId);
}