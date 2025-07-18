drop view public.prescription_drugs_v;

create view public.prescription_drugs_v
as
select
    p.*, (m.name || ' (' || m.generic_name || ')') as medicineName,
    m.expiring,
    m.runningLow,
    m.quantity as stockQuantity,
    least(m.quantity, p.quantity) as actualQuantity,
    m.selling_price,
    (least(m.quantity, p.quantity) * m.selling_price) totalCost
from
    prescription_drugs p
join
    medicines m on m.id = p.medicine_id;