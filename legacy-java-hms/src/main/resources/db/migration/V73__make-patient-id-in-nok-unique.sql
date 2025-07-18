ALTER TABLE patient_nok ADD UNIQUE (patientId);

insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
    ('ReadPatientNok', 'View patient next of kin', 'Permission to view patient next of kin.', false),
    ('WritePatientNok', 'Edit patient next of kin', 'Permission to update patient next of kin details.', false);

insert into role_permission (roleKey, permissionKey)
values ('Administrator', 'ReadPatientNok'), ('Administrator', 'WritePatientNok');