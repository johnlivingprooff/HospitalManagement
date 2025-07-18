insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('ViewPatientBirths', 'View patient births', 'Permission to view patient''s registered births.', false),
('WritePatientBirths', 'Register patient births', 'Permission to to register births.', false);

insert into role_permission(roleKey, permissionKey)
values
('Administrator', 'ViewPatientBirths'),
('Administrator', 'WritePatientBirths');