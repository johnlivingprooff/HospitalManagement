DROP VIEW public.low_stock_medicines;
DROP VIEW public.expiring_medicines;
DROP VIEW public.medicines;

ALTER TABLE medicine
    ALTER COLUMN location TYPE bigint USING location::bigint,
    ALTER COLUMN location SET NOT NULL,
    ADD CONSTRAINT locationfk FOREIGN KEY(location) references medicine_location(id) MATCH FULL;

CREATE VIEW public.medicines
 AS
SELECT s.*,
(SELECT exists (SELECT id from medicine_expiration where s.daysToExpiration <= days)) as expiring
FROM (
	SELECT m.*,
		c.name AS categoryName, l.name AS locationName,
		DATE_PART('day', m.expires::date::timestamp - now()::timestamp)::bigint as daysToExpiration,
		(quantity <= threshold) as runningLow
FROM medicine m
JOIN medicine_category c ON c.id = m.category
JOIN medicine_location l ON l.id = m.location
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