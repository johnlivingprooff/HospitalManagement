create function terminateAdmission(
	in admissionId bigint,
	in _reason text,
	in _attachment text,
	in termination_date timestamptz,
	in status text,
	in terminatedBy bigint
)
returns int
language plpgsql
as $$
begin

update admissions
set
	discharged_at = $4,
	termination_reason = $2,
	terminated_by = $6,
	termination_attachment = $3,
	status = $5
where id = $1;

update beds
set vacant = true
where id = (select bed_id from admissions where id = $1);

    RETURN 1;
end;
$$;

create function getAdmissionFee(in admissionId bigint)
returns numeric
language plpgsql
as $$
declare
    _balance numeric;
begin
select r.rate * a.duration into _balance
from (
	select
	case
		when (admission_type = 'FullAdmission') then (select(EXTRACT(EPOCH FROM discharged_at) - EXTRACT(EPOCH FROM created_at))/3600)
		when (admission_type = 'ShortStay') then (select abs(discharged_at::date - created_at::date))
	end as duration
	from admissions
	where id = $1
) as a, (
	select rate from admission_rates where admission_type = (select admission_type from admissions where id = $1)
) as r;
return _balance;
end;
$$;

create function createAdmissionBill (
	in admissionId bigint,
	in _patient_id bigint,
	in _status text,
	in _bill_type text,
	in _created_at timestamptz,
	in _updated_at timestamptz
)
returns int
language plpgsql
as $$
declare
	bill bigint;
begin

insert into bills (
	balance,
	paid,
	patient_id,
	status,
	bill_type,
	created_at,
	updated_at
)
values ((select getAdmissionFee($1)), 0, $2, $3, $4, $5, $6)
returning id into bill;

insert into admission_bills (bill_id, admission_id) values (bill, $1);

return 1;

end;
$$;