create table surgeries (
    id bigserial primary key,
    procedure_id bigint references procedures(id),
    patient_id bigint references patient(id),
    performed_by bigint references account(id),
    created_at timestamptz not null,
    notes text,
    attachment text
);

create table surgical_bills (
    id bigserial primary key,
    bill_id bigint references bills (id),
    surgery_id bigint references surgeries(id)
);

create view public.surgeries_v
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


create function addMedicalSurgery (
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

insert into surgeries (notes, performed_by, patient_id, procedure_id, attachment, created_at)
    values ($1, $2, $3, $4, $5, $6) returning id into surgery;

insert into bills (balance, paid, patient_id, status, bill_type, created_at, updated_at)
    values ((select cost from procedures_v where id = $4), 0.0, $3, $8, $7, $6, $6) returning id into bill;

insert into surgical_bills (bill_id, surgery_id) values (bill, surgery);

return surgery;
end;
$BODY$;

insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('AccessTheater', 'Access Theater Module', 'Permission to access the theater module.', true),
('ReadTheaterProcedures', 'Access Surgical Procedures', 'Permission to access list of theater surgical procedures.', false),
('WriteTheaterProcedures', 'Modify Surgical Procedures', 'Permission to create and modify theater surgical procedures.', false),
('ReadTheaterSurgeries', 'Access Surgery Results', 'Permission to access medical surgery results under the theater module.', false),
('WriteTheaterSurgeries', 'Upload Surgery Results', 'Permission to upload medical surgery results under the theater module.', false);

insert into role_permission(roleKey, permissionKey)
values
('Administrator', 'AccessTheater'),
('Administrator', 'ReadTheaterProcedures'),
('Administrator', 'WriteTheaterProcedures'),
('Administrator', 'ReadTheaterSurgeries'),
('Administrator', 'WriteTheaterSurgeries');

insert into permissionDependency (parent, child)
values
('AccessTheater', 'ReadTheaterProcedures'),
('AccessTheater', 'WriteTheaterProcedures'),
('AccessTheater', 'ReadTheaterSurgeries'),
('AccessTheater', 'WriteTheaterSurgeries');