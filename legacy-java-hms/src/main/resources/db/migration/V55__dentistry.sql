create table dental_surgeries (
    id bigserial primary key,
    procedure_id bigint references patient(id),
    patient_id bigint references patient(id),
    performed_by bigint references patient(id),
    date_created timestamptz not null,
    notes text,
    attachment text
);

create table dental_bills (
    id bigserial primary key,
    bill_id bigint references bills (id),
    surgery_id bigint references dental_surgeries(id)
);

insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('AccessDentistry', 'Access Dentistry Module', 'Permission to access the dentistry module.', true),
('AccessDentalProcedures', 'Access Dental Procedures', 'Permission to access list of dental procedure.', false),
('WriteDentalProcedures', 'Modify Dental Procedures', 'Permission to create and modify dental procedures.', false),
('ReadDentalSurgeries', 'Access Dental Surgery Results', 'Permission to access dental surgery results.', false),
('WriteDentalSurgeries', 'Upload Dental Surgery Results', 'Permission to upload dental surgery results.', false);

insert into role_permission(roleKey, permissionKey)
values
('Administrator', 'AccessDentistry'),
('Administrator', 'AccessDentalProcedures'),
('Administrator', 'WriteDentalProcedures'),
('Administrator', 'ReadDentalSurgeries'),
('Administrator', 'WriteDentalSurgeries');

insert into permissionDependency (parent, child)
values
('AccessDentistry', 'AccessDentalProcedures'),
('AccessDentistry', 'WriteDentalProcedures'),
('AccessDentistry', 'ReadDentalSurgeries'),
('AccessDentistry', 'WriteDentalSurgeries');

create view public.dental_surgeries_v
as
select t.*,
     d.name as department,
    (p.firstName || ' ' || p.lastName) as patientName,
    p.mrn as patientMrn,
    (a.firstName || ' ' || a.lastName) as performer,
    proc.name as procedureName
from dental_surgeries t
join account a on a.id = t.performed_by
join department d on d.id = a.departmentId
join patient p on p.id = t.patient_id
join procedures proc on proc.id = t.procedure_id;


create function addDentalSurgery (
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
    surgery bigint;
begin

insert into dental_surgeries (notes, performed_by, patient_id, procedure_id, attachment, created_at)
    values ($1, $2, $3, $4, $5, $6) returning id into surgery;

insert into bills (balance, paid, patient_id, status, bill_type, created_at, updated_at)
    values ((select cost from procedures_v where id = $4), 0.0, $3, $8, $7, $6, $6) returning id into bill;

insert into dental_bills (bill_id, surgery_id) values (bill, surgery);

return surgery;
end;
$BODY$;