package app.models.patient;

import org.skife.jdbi.v2.ReflectionBeanMapper;

public class PatientIdsMapper extends ReflectionBeanMapper<PatientIds> {

    public PatientIdsMapper() {
        super(PatientIds.class);
    }
}
