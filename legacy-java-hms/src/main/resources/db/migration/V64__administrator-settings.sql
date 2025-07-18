insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('WriteAdminSettings', 'Modify System Administrator Information',
    'Permission to modify system administrator information', true);

insert into permissionDependency (parent, child)
values
('ReadSystem', 'WriteAdminSettings');

insert into role_permission(roleKey, permissionKey)
values
('Administrator', 'WriteAdminSettings');