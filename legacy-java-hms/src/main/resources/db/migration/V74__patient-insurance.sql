ALTER TABLE patient_insurance
    add column address text not null,
    add column phone text not null,
    drop column notes,
    ADD UNIQUE (patientId);

insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
    ('ReadPatientInsurance', 'View patient insurance', 'Permission to view patient insurance details.', false),
    ('WritePatientInsurance', 'Edit patient insurance', 'Permission to update patient insurance details.', false);

insert into role_permission (roleKey, permissionKey)
values ('Administrator', 'ReadPatientInsurance'), ('Administrator', 'WritePatientInsurance');