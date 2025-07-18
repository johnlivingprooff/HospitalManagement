insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('ReadAccounts', 'Access System Accounts', 'Allows read-only access to system accounts', false),
('WriteAccounts', 'Modify System Accounts', 'Allows modifying system accounts', false);

insert into role_permission(roleKey, permissionKey)
values
('Administrator', 'ReadAccounts'),
('Administrator', 'WriteAccounts');