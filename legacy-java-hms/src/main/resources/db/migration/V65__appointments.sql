drop table if exists appointments;

create table appointments (
    id bigserial primary key,
    patient_id bigint not null references patient (id),
    doctor_id bigint not null references account(id),
    scheduled_at timestamptz not null,
    created_at timestamptz not null,
    cancelled boolean not null,
    completed boolean not null,
    details text,
    cancel_reason text,
    created_by bigint not null references account(id)
);

create view appointments_v
as
select a.*,
    p.mrn as patientMrn,
    (p.firstName || ' ' || p.lastName) as patientName,
    (acc1.firstName || ' ' || acc1.lastName) as doctorName,
    (acc2.firstName || ' ' || acc2.lastName) as receptionistName,
    dept.name as department,
    ((cancelled is false) and (completed is false)) as active
from appointments a
join patient p on p.id = a.patient_id
join account acc1 on acc1.id = a.doctor_id
join account acc2 on acc2.id = a.created_by
join department dept on dept.id = acc1.departmentId;


create view completed_appointments_v
as
select *
from appointments_v
where completed = true;


create view cancelled_appointments_v
as
select *
from appointments_v
where cancelled = true;


create view active_appointments_v
as
select *
from appointments_v
where active = true;


insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('AccessAppointments', 'Access Appointments Module', 'Permission to access appointments module.', true),
('ReadAppointments', 'View Appointments', 'Permission to create appointments.', false),
('WriteAppointments', 'Create Appointments', 'Permission to create appointments.', false),
('WriteSelfAppointments', 'Create Self Appointments', 'Permission to create self appointments.', true);

insert into role_permission(roleKey, permissionKey)
values
('Administrator', 'AccessAppointments'),
('Administrator', 'ReadAppointments'),
('Administrator', 'WriteAppointments'),
('Administrator', 'WriteSelfAppointments');

insert into permissionDependency (parent, child)
values
('AccessAppointments', 'ReadAppointments'),
('AccessAppointments', 'WriteAppointments'),
('AccessAppointments', 'WriteSelfAppointments');
