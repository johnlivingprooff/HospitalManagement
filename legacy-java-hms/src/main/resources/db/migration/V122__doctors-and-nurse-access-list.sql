update permission
set permissionName = 'Access Doctors List',
    privileged = false,
    permissionDescription = 'Permission to access list of doctors and view their schedule and appointments.'
where permissionKey = 'AccessDoctors';

update permission
set permissionName = 'Access Nurses List',
    privileged = false,
    permissionDescription = 'Permission to access list of all available nurses in the system.'
where permissionKey = 'AccessNurses';

