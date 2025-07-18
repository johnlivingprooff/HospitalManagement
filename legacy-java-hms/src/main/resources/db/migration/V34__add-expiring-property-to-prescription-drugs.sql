drop view public.prescription_drugs_v;

create view public.prescription_drugs_v
as
select p.*, (m.name || ' (' || m.generic_name || ')') as medicineName, m.expiring, m.runningLow
from prescription_drugs p
join medicines m on m.id = p.medicine_id;