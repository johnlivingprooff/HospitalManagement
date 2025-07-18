drop view admissions_v;

alter table admissions
    add column reason text not null default '',
    add column attachment text;

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