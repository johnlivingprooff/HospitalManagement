-- Permissions and role
insert into role (roleKey, roleName, roleDescription, systemRole, privileged, created, modified)
values
('Nurse', 'Nurse', 'Access to nurses status for collecting patient vitals', true, false, now(), now());


insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('AccessVitalsModule', 'Access Vitals Module', 'Grants access to the nurses station module', true),
('ReadVitals', 'Access Vitals', 'Allows read-only access to patient vitals', true),
('WriteVitals', 'Add Vitals', 'Allows enter patient vitals data to the system', true);

insert into role_permission(roleKey, permissionKey)
values
('Nurse', 'AccessVitalsModule'),
('Nurse', 'ReadVitals'),
('Nurse', 'WriteVitals');

insert into permissionDependency (parent, child)
values
('AccessVitalsModule', 'ReadVitals'),
('AccessVitalsModule', 'WriteVitals');