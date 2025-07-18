insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('ReadRegions', 'Access Regions', 'Access list of regions', false),
('WriteRegions', 'Modify Regions', 'Modify regions', false),

('ReadWorkstations', 'Access Work Stations', 'Access list of work stations', false),
('WriteWorkstations', 'Modify Work Stations', 'Modify work stations', false),

('ReadDistricts', 'Access Districts', 'Access list of districts', false),
('WriteDistricts', 'Modify Districts', 'Modify districts', false),

('ReadDepartments', 'Access Departments', 'Access list of departments', false),
('WriteDepartments', 'Modify Departments', 'Modify departments', false);

insert into role_permission(roleKey, permissionKey)
values
('Administrator', 'ReadRegions'),
('Administrator', 'WriteRegions'),

('Administrator', 'ReadWorkstations'),
('Administrator', 'WriteWorkstations'),

('Administrator', 'ReadDistricts'),
('Administrator', 'WriteDistricts'),

('Administrator', 'ReadDepartments'),
('Administrator', 'WriteDepartments');