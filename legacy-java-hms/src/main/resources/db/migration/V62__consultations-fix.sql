drop function if exists addConsultationResults;

create function addConsultationResults (
    in _notes text,
    in _performer bigint,
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
    consultation bigint;
begin

insert into consultation_results (notes, performed_by, patient_id, procedure_id, attachment, created_at)
    values ($1, $2, $3, $4, $5, $6) returning id into consultation;

insert into bills (balance, paid, patient_id, status, bill_type, created_at, updated_at)
    values ((select cost from procedures_v where id = $4), 0.0, $3, $8, $7, $6, $6) returning id into bill;

insert into consultation_bills (bill_id, consultation_id) values (bill, consultation);

return surgery;
end;
$BODY$;