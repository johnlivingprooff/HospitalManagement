create table consultation_results (
    id bigserial primary key,
    procedure_id bigint references procedures(id),
    patient_id bigint references patient(id),
    performed_by bigint references account(id),
    created_at timestamptz not null,
    notes text,
    attachment text
);

create table consultation_bills (
    id bigserial primary key,
    bill_id bigint references bills (id),
    consultation_id bigint references consultation_results(id)
);

create view public.consultation_results_v
as
select t.*,
     d.name as department,
    (p.firstName || ' ' || p.lastName) as patientName,
    p.mrn as patientMrn,
    (a.firstName || ' ' || a.lastName) as performer,
    proc.name as procedureName
from consultation_results t
join account a on a.id = t.performed_by
join department d on d.id = a.departmentId
join patient p on p.id = t.patient_id
join procedures proc on proc.id = t.procedure_id;


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

insert into consultation_bills (bill_id, surgery_id) values (bill, consultation);

return surgery;
end;
$BODY$;

insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('ReadConsultationResults', 'Access Consultation Results', 'Permission to access consultation results.', false),
('WriteConsultationResults', 'Upload Consultation Results', 'Permission to upload patient consultation results.', false);

insert into role_permission(roleKey, permissionKey)
values
('Administrator', 'AccessConsultations'),
('Administrator', 'ReadConsultations'),
('Administrator', 'WriteConsultations'),
('Administrator', 'ReadConsultationResults'),
('Administrator', 'WriteConsultationResults');

insert into permissionDependency (parent, child)
values
('AccessConsultations', 'ReadConsultations'),
('AccessConsultations', 'WriteConsultations'),
('AccessConsultations', 'ReadConsultationResults'),
('AccessConsultations', 'WriteConsultationResults');