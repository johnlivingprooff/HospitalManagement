drop view if exists public.lab_tests_v;

create view public.lab_tests_v
as
select t.*,
     d.name as department,
    (p.firstName || ' ' || p.lastName) as patientName,
    p.mrn as patientMrn,
    (a.firstName || ' ' || a.lastName) as examinerName,
    proc.name as procedureName
from lab_tests t
join account a on a.id = t.examiner
join department d on d.id = a.departmentId
join patient p on p.id = t.patient_id
join procedures proc on proc.id = t.procedure_id