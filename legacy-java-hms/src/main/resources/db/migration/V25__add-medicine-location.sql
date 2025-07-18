create table if not exists medicine_location (
    id bigserial primary key,
    name text not null,
    deleted boolean not null
);

-- permissions
insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('ReadMedicineLocations', 'Access Medicine Locations', 'Ability to access list of medicine locations.', false),
('WriteMedicineLocations', 'Modify Medicine Locations', 'Ability to add and update medicine locations.', false);

insert into role_permission(roleKey, permissionKey)
values
('Administrator', 'ReadMedicineLocations'),
('Administrator', 'WriteMedicineLocations');

insert into permissionDependency (parent, child)
values
('AccessPharmacy', 'ReadMedicineLocations'),
('AccessPharmacy', 'WriteMedicineLocations');