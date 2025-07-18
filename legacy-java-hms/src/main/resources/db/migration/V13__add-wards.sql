create table if not exists wards (
    id bigserial primary key,
    code text unique not null,
    "name" text not null,
    active boolean not null
);

-- permissions
insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('ReadWards', 'Access Wards', 'Ability to access list of wards', false),
('WriteWards', 'Modify Wards', 'Ability to add and update wards', false);

insert into role_permission(roleKey, permissionKey)
values
('Administrator', 'ReadWards'),
('Administrator', 'WriteWards');

insert into permissionDependency (parent, child)
values
('ReadSettings', 'ReadWards'),
('ReadSettings', 'WriteWards');