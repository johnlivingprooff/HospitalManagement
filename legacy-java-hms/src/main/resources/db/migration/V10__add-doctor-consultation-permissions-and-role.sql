-- Permissions and role
insert into role (roleKey, roleName, roleDescription, systemRole, privileged, created, modified)
values
('Doctor', 'Doctor', 'Doctor role with access to the consultation module only', true, false, now(), now());


insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('AccessConsultations', 'Access Docotor Consultation Module', 'Grants access to the doctor consultations module', true),
('ReadConsultations', 'Access Consultations', 'Allows read-only access to consultation list', true),
('WriteConsultations', 'Modify Consultations', 'Allows collecting and capturing patient data under consultations module', true);

insert into role_permission(roleKey, permissionKey)
values
('Doctor', 'AccessConsultations'),
('Doctor', 'ReadConsultations'),
('Doctor', 'WriteConsultations');

insert into permissionDependency (parent, child)
values
('AccessConsultations', 'ReadConsultations'),
('AccessConsultations', 'WriteConsultations');


-- Add column
alter table account add column role_modifiable boolean;

update account set role_modifiable = false where hidden = true or "system" = true;
update account set role_modifiable = true where hidden = false and "system" = false;

alter table account alter column role_modifiable set not null;