insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('ReadAdmissionDetails', 'Access Admission Details', 'Permission to access patient admission details.', false);

insert into role_permission(roleKey, permissionKey)
values
('Administrator', 'ReadAdmissionDetails');

insert into permissionDependency (parent, child)
values
('AccessAdmissions', 'ReadAdmissionDetails');