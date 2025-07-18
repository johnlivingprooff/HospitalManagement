create table lab_tests (
    id bigserial primary key,
    notes text not null,
    examiner bigint not null references account (id),
    patient_id bigint not null references patient (id),
    procedure_id bigint not null references procedures (id),
    attachment text,
    created_at timestamptz not null
);

create table lab_bills (
    id bigserial primary key,
    bill_id bigint not null references bills(id),
    lab_test_id bigint not null references lab_tests (id)
);

create view public.lab_tests_v
as
select t.*,
     d.name as department,
    (p.firstName || ' ' || p.lastName) as patientName,
    p.mrn as patientMrn,
    (a.firstName || ' ' || a.lastName) as examinerName
from lab_tests t
join account a on a.id = t.examiner
join department d on d.id = a.departmentId
join patient p on p.id = t.patient_id;


create function addLabTest (
    in _notes text,
    in _examiner bigint,
    in _patient_id bigint,
    in _procedure_id bigint,
    in _attachment text,
    in _created_at timestamptz,
    in _bill_type text,
    in _bill_status text
)
    RETURNS bigint
    LANGUAGE 'plpgsql'

AS $BODY$
declare
    bill bigint;
    test_id bigint;
begin

insert into lab_tests (notes, examiner, patient_id, procedure_id, attachment, created_at)
    values ($1, $2, $3, $4, $5, $6) returning id into test_id;

insert into bills (balance, paid, patient_id, status, bill_type, created_at, updated_at)
    values ((select cost from procedures_v where id = $4), 0.0, $3, $8, $7, $6, $6) returning id into bill;

insert into lab_bills (bill_id, lab_test_id) values (bill, test_id);

return test_id;
end;
$BODY$;
