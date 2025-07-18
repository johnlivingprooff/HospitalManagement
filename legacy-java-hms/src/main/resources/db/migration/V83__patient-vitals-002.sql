drop view patient_vitals_v;

alter table patient_vitals
    add column tempUnits text not null default 'Celsius';


create view patient_vitals_v
as
select v.*, (a.firstName || ' ' || a.lastName) as examiner
from patient_vitals v
join account a on a.id = v.created_by;