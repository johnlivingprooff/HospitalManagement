insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('AccessLabs', 'Access Laboratory Module', 'Permission to access the lab module.', true),
('ReadLabProcedures', 'Access Lab Tests', 'Permissions to access lab tests.', false),
('WriteLabProcedures', 'Modify Lab Tests', 'Permission to create and modify lab tests.', false),
('ReadLabTests', 'Access Lab Tests', 'Permission to access lab test results.', false),
('WriteLabTests', 'Administer Lab Tests', 'Permission to create and upload lab test results.', false);

insert into role_permission(roleKey, permissionKey)
values
('Administrator', 'AccessLabs'),
('Administrator', 'ReadLabProcedures'),
('Administrator', 'WriteLabProcedures'),
('Administrator', 'ReadLabTests'),
('Administrator', 'WriteLabTests');

insert into permissionDependency (parent, child)
values
('AccessLabs', 'ReadLabProcedures'),
('AccessLabs', 'WriteLabProcedures'),
('AccessLabs', 'ReadLabTests'),
('AccessLabs', 'WriteLabTests');