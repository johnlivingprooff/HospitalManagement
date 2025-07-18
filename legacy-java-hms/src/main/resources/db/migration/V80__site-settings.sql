insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('WriteSystemSettings', 'Update Website Settings', 'Permission to update website settings.', true);

insert into role_permission(roleKey, permissionKey)
values
('Administrator', 'WriteSystemSettings');