drop view if exists public.surgeries_v;

create view public.surgeries_v
as
select t.*,
     d.name as department,
    (p.firstName || ' ' || p.lastName) as patientName,
    p.mrn as patientMrn,
    (a.firstName || ' ' || a.lastName) as performer,
    proc.name as procedureName
from surgeries t
join account a on a.id = t.performed_by
join department d on d.id = a.departmentId
join patient p on p.id = t.patient_id
join procedures proc on proc.id = t.procedure_id;