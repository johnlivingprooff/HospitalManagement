create table medicine_expiration (
    id bigserial primary key,
    days int not null
);

insert into medicine_expiration (days) values (14);

DROP VIEW public.medicines;

CREATE VIEW public.medicines
 AS
SELECT s.*,
(SELECT exists (SELECT id from medicine_expiration where s.daysToExpiration <= days)) as expiring
FROM (
	SELECT m.*,
		c.name AS categoryName,
		DATE_PART('day', m.expires::date::timestamp - now()::timestamp)::bigint as daysToExpiration,
		(quantity <= threshold) as runningLow
FROM medicine m
JOIN medicine_category c ON c.id = m.category
WHERE m.deleted = false
) s;

-- View for expiring medicine
CREATE VIEW public.expiring_medicines
 AS
 SELECT *
 FROM medicines
 WHERE expiring = true;

-- View for low stock medicine
CREATE VIEW public.low_stock_medicines
 AS
 SELECT *
 FROM medicines
 WHERE runningLow = true;