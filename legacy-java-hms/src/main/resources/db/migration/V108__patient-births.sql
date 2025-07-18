CREATE TABLE public.births (
	id bigserial NOT NULL primary key,
	name text not null,
	patient_id bigint NOT NULL references patient(id),
	dob timestamptz not null,
	created timestamptz NOT NULL,
	pulse integer NOT NULL,
	breaths integer NOT NULL,
	height numeric(4,2) NOT NULL,
	weight numeric(4,2) NOT NULL,
	temperature numeric(4,2) NOT NULL,
	systolic integer NOT NULL,
	diastolic integer NOT NULL,
	created_by bigint NOT NULL REFERENCES account(id),
	tempunits text NOT NULL
);


create view births_v
as
select b.*,
    (p.firstName || ' ' || p.lastName) as patient,
    (a.firstName || ' ' || a.lastName) as createdBy
from births b
join patient p on p.id = b.patient_id
join account a on a.id = b.created_by