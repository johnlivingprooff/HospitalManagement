delete from permissionDependency
where child = 'EditPatients';

delete from role_permission
where permissionKey = 'EditPatients';

delete from permission
where permissionKey = 'EditPatients';