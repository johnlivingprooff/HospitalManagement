delete from role_permission where permissionKey in ('ViewPatientAllergies', 'AddPatientAllergies');
delete from permission where permissionKey in ('ViewPatientAllergies', 'AddPatientAllergies');