package app.models.account;

import org.skife.jdbi.v2.ReflectionBeanMapper;


public class AccountMapper extends ReflectionBeanMapper<Account> {

    public AccountMapper() {
        super(Account.class);
    }
}
