insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('ReadDeathReports', 'Access Death Reports', 'Permission to access list of death reports.', false);

insert into role_permission(roleKey, permissionKey)
values
('Administrator', 'ReadDeathReports');

insert into permissionDependency(parent, child)
values
('ReadPatients', 'ReadDeathReports');