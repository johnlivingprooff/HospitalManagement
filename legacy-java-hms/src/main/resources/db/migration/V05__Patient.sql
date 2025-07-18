
insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('ReadPatients', 'Access Patients Module', 'Permission to access the Patient Management Module', true),
('ViewInPatients', 'Access Inpatients', 'Permission to access inpatient records', false),
('ViewOutPatients', 'Access Outpatients', 'Permission to access outpatient records', false),
('ViewAdmittedPatients', 'Access Admitted Patients', 'Permission to view admitted patients', false),
('ViewDischargedPatients', 'Access Discharged Patients', 'Permission to view discharged patients', false),
('AddPatients', 'Register Patients', 'Permission to add patients into the system', false),
('EditPatients', 'Edit Patients', 'Permission edit patient information', false),
('ViewPatientVitals', 'View Patient Vitals', 'Permission to access patient vitals records', false),
('EditPatientVitals', 'Record Patient Vitals', 'Permission to add patient vitals records', false),
('ViewPatientLabTests', 'View Patient Lab Tests', 'Permission to access patient lab test results', false),
('AddPatientLabTests', 'Add Patient Labe Tests', 'Permission to add patient lab test results', false),
('ViewPatientNoks', 'View Patient Next Of Kins', 'Permission to acccess patient''s next of kin information', false),
('ModifyPatientNoks', 'Modify Patient Next of Kins', 'Permission to add and edit patient''s next of kin information', false),
('ViewPatientAllergies', 'View Patient Allergies', 'Permission to view patient allergies', false),
('AddPatientAllergies', 'Add Patient Allergies', 'Permission to add patient allergies', false),
('ViewPatientAttachments', 'View Patient Attachments', 'Permission to view patient attachments uploaded onto the sytem', false),
('AddPatientAttachments', 'Add Patient Attachments', 'Permission to upload documents under patients', false),
('EditPatientAttachments', 'Edit Patient Attachments', 'Permission to edit (update or delete) patient attachments', false),
('AddPatientVisits', 'Add Patient Visits', 'Permission to file patient visits', false),
('ViewPatientVisits', 'View Patient Visits', 'Permission to view patient visit histotry', false),
('DischargePatients', 'Discharge Patients', 'Permission to file patient discharges', false);

insert into permissionDependency (parent, child)
values
('ReadPatients', 'ViewOutPatients'),
('ReadPatients', 'ViewAdmittedPatients'),
('ReadPatients', 'ViewDischargedPatients'),
('ReadPatients', 'ViewInPatients'),
('ReadPatients', 'AddPatients'),
('ReadPatients', 'EditPatients'),
('ReadPatients', 'DischargePatients');

insert into role (roleKey, roleName, roleDescription, systemRole, privileged, created, modified)
values
('Receptionist', 'Front Desk', 'Responsible for handling basic front desk tasks', true, false, now(), now());

insert into role_permission(roleKey, permissionKey)
values


('Receptionist', 'ReadPatients'),
('Receptionist', 'AddPatients'),
('Receptionist', 'ViewDischargedPatients'),
('Receptionist', 'ViewAdmittedPatients'),
('Receptionist', 'ViewInPatients'),
('Receptionist', 'ViewOutPatients');