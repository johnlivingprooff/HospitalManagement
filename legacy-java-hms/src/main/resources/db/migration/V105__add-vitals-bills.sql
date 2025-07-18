create table vitals_bills (
    id bigserial primary key,
    bill_id bigint not null references bills(id),
    vitals_id bigint not null references patient_vitals (id)
);

create function addVitals (
    in _patient_id bigint,
    in _created_at timestamptz,
    in _vitals_type text,
    in _pulse integer,
    in _breaths integer,
    in _temperature numeric,
    in _systolic integer,
    in _diastolic integer,
    in _created_by bigint,
    in _tempUnits text,

    in _bill_type text,
    in _bill_status text
)
    RETURNS bigint
    LANGUAGE 'plpgsql'

AS $BODY$
declare
    bill bigint;
    vitals_id bigint;
begin

insert into patient_vitals (patient_id, created, type, admission_id, pulse, breaths, temperature, systolic, diastolic, created_by, tempUnits)
    values ($1, $2, $3, null, $4, $5, $6, $7, $8, $9, $10) returning id into vitals_id;

insert into bills (balance, paid, patient_id, status, bill_type, created_at, updated_at)
    values ((select fee from vitals_fee limit 1), 0.0, $1, $12, $11, $2, $2) returning id into bill;

insert into vitals_bills (bill_id, vitals_id) values (bill, vitals_id);

return vitals_id;
end;
$BODY$;
