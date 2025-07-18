insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('PerformPatientActivities', 'Exclusive permission for patients.', 'Grants access to all patient features.', true);

-- Roles. System roles cannot be deleted
insert into role (roleKey, roleName, roleDescription, systemRole, privileged, created, modified)
values
('Patient', 'Patient Role', 'Exclusive role for patients.', true, true, now(), now());

insert into role_permission(roleKey, permissionKey)
values
('Patient', 'PerformPatientActivities');