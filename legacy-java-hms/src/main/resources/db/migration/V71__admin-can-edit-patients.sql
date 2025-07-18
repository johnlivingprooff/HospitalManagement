insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values ('WritePatients', 'Edit Patient Details', 'Permission to edit patient details.', false);

insert into role_permission (roleKey, permissionKey)
    values('Administrator', 'WritePatients');