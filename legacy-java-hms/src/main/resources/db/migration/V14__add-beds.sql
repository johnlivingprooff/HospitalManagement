create table if not exists beds (
    id bigserial primary key,
    code text unique not null,
    ward_id bigint references wards(id) not null,
    vacant boolean not null
);

-- permissions
insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('ReadBeds', 'Access Beds', 'Ability to access list of beds', false),
('WriteBeds', 'Modify Beds', 'Ability to add and update beds', false);

insert into role_permission(roleKey, permissionKey)
values
('Administrator', 'ReadBeds'),
('Administrator', 'WriteBeds');

insert into permissionDependency (parent, child)
values
('ReadSettings', 'ReadBeds'),
('ReadSettings', 'WriteBeds');