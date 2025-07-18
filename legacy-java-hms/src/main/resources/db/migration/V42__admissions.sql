create table admissions (
    id bigserial primary key,
    patient_id bigint not null references patient(id),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    discharged_at timestamptz,
    bed_id bigint not null references beds(id),
    admitted_by bigint not null references account(id),
    admission_type text not null,
    status text not null
);

create view admissions_v
as
select
    a.*,
    w.code as wardCode,
    w.name as wardName,
    b.code as bed,
    p.mrn as patientMrn,
    (p.firstName || ' ' || p.lastName) as patientName
from admissions a
join beds b on b.id = a.bed_id
join wards w on w.id = b.ward_id
join account acc on acc.id = a.admitted_by
join patient p on p.id = a.patient_id;

create table admission_rates (
    id bigserial primary key,
    admission_type text not null unique,
    rate numeric not null
);
insert into admission_rates (admission_type, rate)
    values ('ShortStay', 0), ('FullAdmission', 0);

create table admission_bills (
    id bigserial primary key,
    bill_id bigint unique not null references bills(id),
    admission_id bigint unique not null references admissions(id)
);

insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('AccessAdmissions', 'Access Admissions Module', 'Ability to access admissions module.', true),
('ReadAdmissions', 'Access Admissions', 'Permissions to access admissions.', false),
('WriteAdmissions', 'Admit Patients', 'Permission to create admissions for patients.', false),
('WriteAdmissionRates', 'Modify Admission Rates', 'Permission to adjust short and full admission rates.', false);

insert into role_permission(roleKey, permissionKey)
values
('Administrator', 'AccessAdmissions'),
('Administrator', 'ReadAdmissions'),
('Administrator', 'WriteAdmissions'),
('Administrator', 'WriteAdmissionRates');

insert into permissionDependency (parent, child)
values
('AccessAdmissions', 'ReadAdmissions'),
('AccessAdmissions', 'WriteAdmissions'),
('AccessAdmissions', 'WriteAdmissionRates');