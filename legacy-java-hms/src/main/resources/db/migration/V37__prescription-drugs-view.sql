-- Unlike the medicines view, this view lists all medicines (deleted and active)
create view public.medicines_v
as
 SELECT s.*,
    (SELECT (EXISTS ( SELECT medicine_expiration.id
                   FROM medicine_expiration
                  WHERE s.daysToExpiration <= medicine_expiration.days)) AS "exists") AS expiring
   FROM ( SELECT m.*,
            c.name AS categoryName,
            l.name AS locationName,
            date_part('day'::text, m.expires::date::timestamp without time zone - now()::timestamp without time zone)::bigint AS daysToExpiration,
            m.quantity <= m.threshold AS runningLow
           FROM medicine m
             JOIN medicine_category c ON c.id = m.category
             JOIN medicine_location l ON l.id = m.location
         ) s
order by s.id asc;


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
    medicines_v m on m.id = p.medicine_id;