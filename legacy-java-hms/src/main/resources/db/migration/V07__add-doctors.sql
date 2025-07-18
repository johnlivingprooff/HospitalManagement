create table if not exists doctors (
    id bigserial primary key,
    account_id bigint unique references account(id) not null,
    created timestamp with time zone not null
);

insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('AccessDoctors', 'Manage Doctors', 'Grants access to the doctors module', true),
('ReadDoctors', 'Access Doctors', 'Allows read-only access to the doctors list', false);
insert into role_permission(roleKey, permissionKey)
values
('Administrator', 'AccessDoctors'),
('Administrator', 'ReadDoctors');

insert into permissionDependency (parent, child)
values
('AccessDoctors', 'ReadDoctors');