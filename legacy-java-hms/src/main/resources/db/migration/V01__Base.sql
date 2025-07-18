--- This little snippet will wipe out the entire schema so be careful.
--- @See https://stackoverflow.com/a/36023359
DO $$ DECLARE
    r RECORD;
BEGIN
    FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public' and tablename not in ('flyway_schema_history')) loop
        EXECUTE 'DROP TABLE IF EXISTS ' || quote_ident(r.tablename) || ' CASCADE';
    END LOOP;
END $$;

create table permission (
    id bigserial primary key,
    permissionKey text unique,
    permissionName text not null,
    permissionDescription text,
    -- Privileged permissions cannot be manually assigned to anyone. They are hardcoded and invisible
    privileged boolean not null
);

create table role (
    id bigserial primary key,
    roleKey text not null unique,
    roleName text not null,
    roleDescription text not null,
    created timestamp with time zone not null,
    modified timestamp with time zone not null,
    systemRole boolean not null,
    active boolean default true,
    -- Privileged roles cannot be manually assigned to anyone. They are hardcoded and invisible
    privileged boolean not null
);

create table section (
    id bigserial primary key,
    sSectionName text not null,
    hidden boolean not null,
    systemSection boolean not null
);

create table region (
    id bigserial primary key,
    code text unique not null,
    "name" text not null,
    created timestamp  with time zone not null,
    modified timestamp  with time zone not null,
    system boolean not null,
    hidden boolean not null,
    active boolean not null
);

create table district (
    id bigserial primary key,
    regionId bigint references region(id),
    code text unique not null,
    "name" text not null,
    created timestamp  with time zone not null,
    modified timestamp  with time zone not null,
    system boolean not null,
    hidden boolean not null,
    active boolean not null
);

create table workstation (
    id bigserial primary key,
    districtId bigint references district(id),
    code text unique not null,
    "name" text not null,
    address text not null,
    created timestamp  with time zone not null,
    modified timestamp  with time zone not null,
    system boolean not null,
    hidden boolean not null,
    active boolean not null
);

create table department (
    id bigserial primary key,
    workstationId bigint references workstation(id),
    code text unique not null,
    "name" text not null,
    created timestamp  with time zone not null,
    modified timestamp  with time zone not null,
    system boolean not null,
    hidden boolean not null,
    active boolean not null
);

create table account (
    id bigserial primary key,
    departmentId bigint references department(id),
    email text not null unique,
    password text not null,
    firstName text not null,
    lastName text not null,
    dob date not null,
    created timestamp with time zone not null,
    modified timestamp with time zone not null,
    picture text,
    sex text not null,
    roleKey text references role (RoleKey),
    hidden boolean not null,
    system boolean not null,
    active boolean not null
);

-- Section to User junction table to avoid circular foreign key references
create table section_head (
    Id bigserial primary key,
    SectionID bigint references section (Id),
    accountId bigint references account (Id)
);

create table role_permission (
    Id bigserial primary key,
    roleKey text references role (roleKey),
    permissionKey text references permission (permissionKey)
);

create table contact_info (
    id bigserial primary key,
    physicalAddress text,
    mailingAddress text,
    phone1 text,
    phone2 text,
    accountId bigint references account (Id)
);

create table audit_log (
    id bigserial primary key,
    logType text,
    userAgent text,
    created timestamp with time zone not null,
    action text not null,
    Archived boolean default false,
    location text,
    actorName text,
    address text not null
);

create table patient (
    id bigserial primary key,
    mrn text unique not null,
    firstName text not null,
    lastName text not null,
    sex text not null,
    dob date not null,
    bloodGroup text not null,
    nationality text not null,
    picture text,
    email text,
    address text,
    phone text,
    crated timestamp with time zone not null,
    modified timestamp with time zone not null,
    createdBy bigint references account(Id) not null,
    "type" text not null,
    status text not null,
    active boolean
);

create table patient_vitals (
    id bigserial primary key,
    patient_id bigint references patient (id) not null,
    vital text not null,
    value text not null,
    created timestamp with time zone not null,
    modified timestamp with time zone not null
);

create table patient_insurance(
    id bigserial primary key,
    insurer text not null,
    membershipId text not null,
    notes text,
    patientId bigint references patient(id),
    created timestamp with time zone not null,
    modified timestamp with time zone not null
);

create table patient_employment(
    Id bigserial primary key,
    employer text not null,
    employeeId text,
    patientId bigint references patient(id),
    created timestamp with time zone not null,
    modified timestamp with time zone not null
);

-- Known patient allergies --
create table patient_allergy (
	id bigserial primary key,
	allergy text not null,
	patientId bigint references patient(id),
	created timestamp with time zone not null,
	modified timestamp with time zone not null
);

-- This table will be useful for looking up patient records for re-visiting clients --
create table patient_id(
    id bigserial primary key,
    patientId bigint references patient (id),
    idType text not null,
    idNumber text unique not null,
    issued date not null,
    expiry date not null,
    created timestamp with time zone not null,
    modified timestamp with time zone not null
);

-- Patient next of kin/guardian --
create table patient_nok (
    id bigserial primary key,
    firstName text not null,
    lastName text not null,
    address text,
    phone1 text,
    phone2 text,
    relationshipType text not null,
    patientID bigint references patient(Id),
    created timestamp with time zone not null,
    modified timestamp with time zone not null
);

create table system_settings (
    -- Customary primary key
    id bigserial primary key,
    logo text not null,
    banner text not null,
    modified date not null,
    modifiedBy bigint references account(id)
);

-- User and patient IDs start in the thousands
alter SEQUENCE patient_id_seq start 1000 restart 1000 minvalue 1000 increment by 3;
alter sequence account_id_seq start 1000 restart 1000 minvalue 1000 increment by 7;

-- Add initial permissions
insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('ReadAuditLogs', 'Access Audit Logs', 'Grants access to system audit logs', false),
('ArchiveAuditLogs', 'Archive Audit Logs', 'Allows archiving audit logs', false),
('ReadPermissions', 'Access Permissions', 'Grants read-only access to list of system permissions', false),
('ReadRoles', 'Access Roles', 'Grants read-only access to list of roles', false),
('WriteRoles', 'Modify Roles', 'Grants ability to add and remove roles', false),
('ReadSettings', 'Access Settings Module', 'Grants access to settings module', true),
('ReadSystem', 'Access System Module', 'Grants access to system module', true),
('ReadVersion', 'System Version Information', 'View system version information', false);

-- Roles. System roles cannot be deleted
insert into role (roleKey, roleName, roleDescription, systemRole, privileged, created, modified)
values
('Administrator', 'Super System Administrator', 'Built-in System Administrator', true, true, now(), now());

-- Role to Permissions mapping table
insert into role_permission(roleKey, permissionKey)
values

-- System Administrator
('Administrator', 'ReadAuditLogs'),
('Administrator', 'ArchiveAuditLogs'),
('Administrator', 'ReadPermissions'),
('Administrator', 'ReadRoles'),
('Administrator', 'WriteRoles'),
('Administrator', 'ReadSettings'),
('Administrator', 'ReadSystem'),
('Administrator', 'ReadVersion');

insert into region (code, "name", created, modified, system, hidden, active)
values ('$SYSRGN$', 'localhost', now(), now(), true, true, false);

insert into district (code, "name", regionId, created, modified, system, hidden, active)
values ('$SYSDSTRCT$', 'localhost', 1, now(), now(), true, true, false);

insert into workstation (code, "name", address, districtId, created, modified, system, hidden, active)
values ('$SYSWS$', 'localhost', 'localhost', 1, now(), now(), true, true, false);

insert into department (code, "name", workstationId, created, modified, system, hidden, active)
values ('$SYSDPT$', 'localhost', 1, now(), now(), true, true, false);

do $$
declare
    AdminUserID BIGINT := 0;
begin
    -- Default admin password
    insert into account(email, password, firstName, lastName, dob, created, modified, sex, departmentId, roleKey, active, hidden, system)
    values('admin@localhost.com', '$2a$12$REplbqZQIaQiM2HM/YC.cuabAIlFl3nIhhDS3zTgPpoQ7.jqXXJXS', 'Admin', 'MiHR',
        NOW(), NOW(), NOW(), 'Male', 1, 'Administrator', true, true, true) returning Id into AdminUserID;

    insert into system_settings(logo, banner, modified, modifiedBy)
    values ('', 'MiHR Hospital Management System', current_date, AdminUserID);
end $$;