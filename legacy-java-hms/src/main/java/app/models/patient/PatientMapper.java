package app.models.patient;

import org.skife.jdbi.v2.ReflectionBeanMapper;

public class PatientMapper extends ReflectionBeanMapper<Patient> {

    public PatientMapper() {
        super(Patient.class);
    }
}
