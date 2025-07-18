create table if not exists prescriptions (
    id bigserial primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    patient_id bigint not null references patient(id) match full,
    filer_id bigint not null references account(id) match full,
    -- When status is dispensed (This column will contain the person that dispensed the medicine)
    updater_id bigint not null references account(id) match full,
    status text not null, -- filed, prescribed, dispensed
    deleted boolean not null
);

create table if not exists prescription_drugs (
    id bigserial primary key,
    prescription_id bigint not null references prescriptions(id) match full,
    medicine_id bigint not null references medicine(id) match full,
    quantity bigint not null,
    notes text
);

insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('ReadPrescriptions', 'Access Prescriptions', 'Ability to access list of prescriptions written by the same person.', false),
('WritePrescriptions', 'Modify Medicine Prescriptions', 'Ability to prescribe and and update prescriptions.', false),
('DispenseMedicine', 'Dispense Medicine', 'Ability to dispense prescribed medicine.', false);

insert into role_permission(roleKey, permissionKey)
values
('Administrator', 'ReadPrescriptions'),
('Administrator', 'WritePrescriptions'),
('Administrator', 'DispenseMedicine');

insert into permissionDependency (parent, child)
values
('AccessPharmacy', 'ReadMedicineLocations'),
('AccessPharmacy', 'WriteMedicineLocations'),
('AccessPharmacy', 'DispenseMedicine');

create view public.prescriptions_v
AS
select pre.*,
    (p.firstName || ' ' || p.lastName) as patientName,
    (a1.firstName || ' ' || a1.lastName) as filedBy,
    (a2.firstName || ' ' || a2.lastName) as updatedBy,
    (select count(id) from prescription_drugs where prescription_id = pre.id) as drugs,
    p.mrn as patientMrn
from prescriptions pre
join patient p on p.id = pre.patient_id
join account a1 on a1.id = pre.filer_id
join account a2 on a2.id = pre.updater_id
where pre.deleted = false;

create view public.prescription_drugs_v
as
select p.*, (m.name || ' (' || m.generic_name || ')') as medicineName
from prescription_drugs p
join medicines m on m.id = p.medicine_id;