delete from permission where permissionKey= 'AddPatientLabTests';

update permission
    set permissionName = 'View Patient Next Of Kin',
        permissionDescription = 'Permission to access patient''s next of kin information.'
    where
        permissionKey = 'ViewPatientNoks';

update permission
    set permissionDescription = 'Permission to access lab test procedures.',
        permissionName = 'Access Lab Test Procedures'
    where
        permissionKey = 'ReadLabProcedures';

update permission
    set permissionDescription = 'Permission to add and modify lab test procedures.',
        permissionName = 'Modify Lab Test Procedures'
    where
        permissionKey = 'WriteLabProcedures';