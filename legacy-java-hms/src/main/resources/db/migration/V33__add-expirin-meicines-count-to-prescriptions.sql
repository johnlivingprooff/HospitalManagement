drop view public.prescriptions_v;

create view public.prescriptions_v
AS
select pre.*,
    (p.firstName || ' ' || p.lastName) as patientName,
    (a1.firstName || ' ' || a1.lastName) as filedBy,
    (a2.firstName || ' ' || a2.lastName) as updatedBy,
    (select count(id) from prescription_drugs where prescription_id = pre.id) as drugs,
	(select count(pd.id)
	 	from prescription_drugs pd
	 	join medicines m on m.id = pd.medicine_id
	 	where pd.prescription_id = pre.id and m.expiring = true
	) as expiringDrugs,
    p.mrn as patientMrn, d.name as department
from prescriptions pre
join patient p on p.id = pre.patient_id
join account a1 on a1.id = pre.filer_id
join account a2 on a2.id = pre.updater_id
join department d on d.id = a1.departmentId
where pre.deleted = false;