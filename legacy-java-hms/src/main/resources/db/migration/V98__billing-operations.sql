drop view if exists outstanding_bills_v;
drop view if exists paid_bills_v;

create view outstanding_bills_v
as
with t as (
	select patient_id, sum(balance) as balance, sum(paid) as paid, count(id) as bill_count
	from bills
	where status = 'UnPaid'
	group by patient_id
)
select t.*, p.mrn, (p.firstName || ' ' || p.lastName) as patient, p.phone
from t
join patient p on p.id = t.patient_id;


create view paid_bills_v
as
with t as (
	select patient_id, sum(balance) as balance, sum(paid) as paid, count(id) as bill_count
	from bills
	where status = 'Paid'
	group by patient_id
)
select t.*, p.mrn, (p.firstName || ' ' || p.lastName) as patient, p.phone
from t
join patient p on p.id = t.patient_id;
