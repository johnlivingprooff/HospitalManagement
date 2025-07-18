insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('ViewPatientBills', 'View patient bills', 'Permission to view patient''s list of bills, paid and outstanding.', false);

insert into role_permission(roleKey, permissionKey)
values
('Administrator', 'ViewPatientBills');