insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('AccessNursesStation', 'Access Nurses Station', 'Permission to access nurses station.', true);

-- Implicit permissions by account type
insert into extra_permissions(account_type, permission_key)
values
('Nurse', 'AccessNursesStation');