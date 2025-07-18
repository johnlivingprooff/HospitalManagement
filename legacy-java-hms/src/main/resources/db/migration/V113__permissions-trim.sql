delete from role_permission where permissionKey in (
    'ViewDischargedPatients',
    'DischargePatients'
);

delete from permissionDependency where child in (
    'ViewDischargedPatients',
    'DischargePatients'
);

delete from permission where permissionKey in (
    'ViewDischargedPatients',
    'DischargePatients'
);