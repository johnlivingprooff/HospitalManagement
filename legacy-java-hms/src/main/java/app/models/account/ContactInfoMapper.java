package app.models.account;

import org.skife.jdbi.v2.ReflectionBeanMapper;

public final class ContactInfoMapper extends ReflectionBeanMapper<ContactInfo> {
    public ContactInfoMapper() {
        super(ContactInfo.class);
    }
}
