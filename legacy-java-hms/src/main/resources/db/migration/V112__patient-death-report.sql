insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('ReadPatientDeathReport', 'Access Patient Death Report', 'Permission to access patient death report.', false),
('WritePatientDeathReport', 'Add Patient Death Report', 'Permission to add patient death report.', false);

insert into role_permission(roleKey, permissionKey)
values
('Administrator', 'ReadPatientDeathReport'),
('Administrator', 'WritePatientDeathReport');

create table death_reports (
    id bigserial not null primary key,
    patient_id bigint not null references patient(id),
    dod timestamptz not null,
    created_at timestamptz not null,
    created_by bigint not null references account(id),
    attachment text not null
);

create view death_reports_v
as
select dp.*,
    p.mrn, p.sex, p.dob,
    (p.firstName || ' ' || p.lastName) patient,
    (a.firstName || ' ' || a.lastName) reporter
from death_reports dp
join patient p on p.id = dp.patient_id
join account a on a.id = dp.created_by;