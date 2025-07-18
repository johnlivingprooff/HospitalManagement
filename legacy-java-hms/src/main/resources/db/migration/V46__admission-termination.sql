insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('TerminateAdmissions', 'Terminate Admissions', 'Permission to terminate patient admissions.', false);

insert into role_permission(roleKey, permissionKey)
values
('Administrator', 'TerminateAdmissions');

insert into permissionDependency (parent, child)
values
('AccessAdmissions', 'TerminateAdmissions');