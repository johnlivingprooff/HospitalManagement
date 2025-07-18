delete from "permission" p WHERE lower(permissionKey) ~ 'visits';

delete from role_permission
where roleKey = 'Administrator'
and
permissionKey not in
(
'ReadAuditLogs',
'ArchiveAuditLogs',
'ReadPermissions',
'ReadRoles',
'WriteRoles',
'ReadSettings',
'ReadSystem',
'ReadVersion',
'ReadAccounts',
'WriteAccounts',
'ReadRegions',
'WriteRegions',
'ReadWorkstations',
'WriteWorkstations',
'ReadDistricts',
'WriteDistricts',
'ReadDepartments',
'WriteDepartments',
'WriteAdminSettings',
'WriteSystemSettings'
);