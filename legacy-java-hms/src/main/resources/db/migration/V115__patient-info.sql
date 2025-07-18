insert into permission (permissionKey, permissionName, permissionDescription, privileged)
values
('ReadPatientAdmissions', 'Access Patient Admissions', 'Permission to access patient admission history.', false),
('ReadPatientPrescriptions', 'Access Patient Prescriptions', 'Permission to access patient prescription history.', false),
('ReadPatientLabResults', 'Access Patient Lab Results', 'Permission to access patient lab test results.', false),
('ReadPatientDentalResults', 'Access Patient Dental Visits', 'Permission to access patient dental history.', false),
('ReadPatientOperations', 'Access Patient Theater Visits', 'Permission to access patient theater visits.', false),
('ReadPatientAppointments', 'Access Patient Appointments', 'Permission to see patient appointments.', false),
('ReadPatientConsultations', 'Access Patient Consultations', 'Permission to access patient consultations.', false);

insert into role_permission(roleKey, permissionKey)
values
('Administrator', 'ReadPatientAdmissions'),
('Administrator', 'ReadPatientPrescriptions'),
('Administrator', 'ReadPatientLabResults'),
('Administrator', 'ReadPatientDentalResults'),
('Administrator', 'ReadPatientOperations'),
('Administrator', 'ReadPatientAppointments'),
('Administrator', 'ReadPatientConsultations');