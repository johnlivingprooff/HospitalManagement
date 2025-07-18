package app.models.patient;

import org.skife.jdbi.v2.ReflectionBeanMapper;

public class IdentificationMapper extends ReflectionBeanMapper<Identification> {

    public IdentificationMapper() {
        super(Identification.class);
    }
}
