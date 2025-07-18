drop view if exists recent_payments;

create view recent_payments
as
select bp.id, p.mrn, (p.firstName || ' ' || p.lastName) as patient,
	(a.firstName || ' ' || a.lastName) as processor,
	bp.amount, bp.payer, bp.created_at, b.bill_type
from bill_payments bp
join bills b on b.id = bp.bill_id
join patient p on p.id = b.patient_id
join account a on a.id = bp.created_by;