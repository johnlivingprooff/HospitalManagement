delete from permissionDependency
where child in
('ViewAdmittedPatients'
,'ViewDischargedPatients'
,'ViewPatientAllergies'
,'AddPatientAllergies'
,'AddPatientVisits'
,'ViewPatientVisits'
,'DischargePatients');

delete from permission where permissionKey
in
('ViewAdmittedPatients'
,'ViewDischargedPatients'
,'ViewPatientAllergies'
,'AddPatientAllergies'
,'AddPatientVisits'
,'ViewPatientVisits'
,'DischargePatients');