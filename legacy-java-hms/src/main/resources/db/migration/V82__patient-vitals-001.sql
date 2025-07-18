alter table patient_vitals
    add column created_by bigint not null references account(id);


create view patient_vitals_v
as
select v.*, (a.firstName || ' ' || a.lastName) as examiner
from patient_vitals v
join account a on a.id = v.created_by;