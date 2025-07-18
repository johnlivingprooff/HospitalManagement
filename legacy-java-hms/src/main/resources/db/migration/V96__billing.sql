insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('AccessBilling', 'Access Billing Module', 'Permission to access the billing module.', true);

insert into role_permission(roleKey, permissionKey)
values
('Administrator', 'AccessBilling');