drop view nurses_v, doctors_v, accounts_v;

create view accounts_v
as
SELECT a.*, r.roleName, r.id roleId, d.name department
FROM account a
JOIN role r ON r.roleKey = a.roleKey
JOIN department d ON d.id = a.departmentId;

create view doctors_v
as
select * from accounts_v
where account_type = 'Doctor';

create view nurses_v
as
select * from accounts_v
where account_type = 'Nurse';

-- No longer needed
drop table doctors, nurses;