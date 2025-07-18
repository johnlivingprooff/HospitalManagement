drop view if exists births_v;

ALTER TABLE births
    ALTER COLUMN height TYPE numeric(20, 2),
    ALTER COLUMN weight TYPE numeric(20, 2),
    ALTER COLUMN temperature TYPE numeric(20, 2);

create view births_v
as
select b.*,
    (p.firstName || ' ' || p.lastName) as patient,
    (a.firstName || ' ' || a.lastName) as registeredBy
from births b
join patient p on p.id = b.patient_id
join account a on a.id = b.created_by