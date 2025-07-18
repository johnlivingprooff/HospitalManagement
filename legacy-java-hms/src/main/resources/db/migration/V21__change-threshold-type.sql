DROP VIEW public.medicines;

ALTER TABLE public.medicine
    ALTER COLUMN threshold TYPE bigint;

CREATE VIEW public.medicines
 AS
 SELECT m.*, c.name AS categoryName
   FROM medicine m
     JOIN medicine_category c ON c.id = m.category
  WHERE m.deleted = false;