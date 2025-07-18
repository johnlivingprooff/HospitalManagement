create table if not exists medicine_category (
    id bigserial primary key,
    name text not null,
    deleted boolean not null
);

create table if not exists medicine (
    id bigserial primary key,
    category bigint not null references medicine_category (id),
    location text not null,
    purchase_price numeric not null,
    selling_price numeric not null,
    quantity bigint not null,
    generic_name text not null,
    updated timestamp with time zone not null,
    expires timestamp with time zone not null,
    deleted boolean not null
);

-- permissions
insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('AccessPharmacy', 'Access Pharmacy Module', 'Grants access to the pharmacy module.', true),
('ReadMedicineCategories', 'Access Medicine Categories', 'Ability to access list of medicine categories.', false),
('WriteMedicineCategories', 'Modify Medicine Categories', 'Ability to add and update medicine categories.', false),
('ReadMedicines', 'Access Medicines', 'Permission to view list of medicines.', false),
('WriteMedicines', 'Modify Medicines', 'Permission to add, updated, and remove medicine from the list.', false);

insert into role_permission(roleKey, permissionKey)
values
('Administrator', 'AccessPharmacy'),
('Administrator', 'ReadMedicineCategories'),
('Administrator', 'WriteMedicineCategories'),
('Administrator', 'ReadMedicines'),
('Administrator', 'WriteMedicines');

insert into permissionDependency (parent, child)
values
('AccessPharmacy', 'ReadMedicineCategories'),
('AccessPharmacy', 'WriteMedicineCategories'),
('AccessPharmacy', 'ReadMedicines'),
('AccessPharmacy', 'WriteMedicines');