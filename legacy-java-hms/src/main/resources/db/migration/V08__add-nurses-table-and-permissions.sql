create table if not exists nurses (
    id bigserial primary key,
    account_id bigint unique references account(id) not null,
    created timestamp with time zone not null
);

insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('AccessNurses', 'Manage Nurses', 'Grants access to the nurses module', true),
('ReadNurses', 'Access Nurses', 'Allows read-only access to the nurses list', false);

insert into role_permission(roleKey, permissionKey)
values
('Administrator', 'AccessNurses'),
('Administrator', 'ReadNurses');

insert into permissionDependency (parent, child)
values
('AccessNurses', 'ReadNurses');