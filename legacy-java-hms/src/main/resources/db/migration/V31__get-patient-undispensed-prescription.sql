create or replace function public."getUndispensedPrescriptionForPatient"
    (in patientId bigint, in status1 text, in status2 text)
    returns record
    language 'sql'
as $BODY$

select pv.*, d.name as department
from prescriptions_v pv
join account a on a.id = pv.filer_id
join department d on d.id = a.departmentId
where patient_id = $1
and (status = $2 or status = $3) limit 1;
$BODY$;