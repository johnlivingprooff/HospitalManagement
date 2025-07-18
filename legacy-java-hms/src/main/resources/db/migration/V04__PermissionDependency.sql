/**
   Synopsis:

   Some features of the system are placed under certain dropdown menus and in order to access some of these menus,
   other permissions are also required.

   This table should be used to lookup which permission is required to access sub-level menu items on the front end.
**/

create table permissionDependency (
	id bigserial primary key,
	parent text not null references permission(permissionKey),
	child text not null references permission(permissionKey)
);

insert into permissionDependency (parent, child)
values
('ReadSettings', 'ReadRegions'),
('ReadSettings', 'ReadWorkstations'),
('ReadSettings', 'ReadDistricts'),
('ReadSettings', 'ReadDepartments'),
('ReadSettings', 'ReadPermissions'),
('ReadSettings', 'ReadRoles'),

('ReadSystem', 'ReadAuditLogs'),
('ReadSystem', 'ReadVersion');