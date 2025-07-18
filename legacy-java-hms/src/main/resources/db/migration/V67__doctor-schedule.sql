create table if not exists schedules (
    id bigserial primary key,
    doctor_id bigint not null references account (id),
    start_date timestamptz not null,
    end_date timestamptz not null
);

drop view if exists schedules_v;
create view schedules_v
as
select s.*, (a.firstName || ' ' || a.lastName) doctorName
from schedules s
join account a on a.id = s.doctor_id;

insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('AccessScheduleModule', 'Access Schedule Module', 'Permission to access the schedule module.', true);