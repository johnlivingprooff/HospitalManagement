alter table account
    add column phone text,
    add column account_type text not null default 'Doctor';

create view accounts_v
as
select a.*, d.name department
from account a
join department d on d.id = a.departmentId;

create view doctors_v
as
select * from accounts_v
where account_type = 'Doctor';

create view nurses_v
as
select * from accounts_v
where account_type = 'Nurse';

-- These permissions will automatically be granted to specific account types
create table extra_permissions (
    id bigserial primary key,
    account_type text not null,
    permission_key text references permission(permissionKey)
);

insert into extra_permissions(account_type, permission_key)
    values
    ('Doctor', 'AccessScheduleModule'),
    ('Doctor', 'AccessDoctors'),
    ('Doctor', 'ReadDoctors'),
    ('Nurse', 'AccessDoctors'),
    ('Nurse', 'ReadDoctors');