alter table patient_vitals
    add column type text not null, -- Admission/WalkIn
    -- If type is Admissions, then this will point to the admission
    add column admission_id bigint references admissions (id);

delete from role_permission where permissionKey like '%Vitals%';
delete from permissionDependency where parent in ('AccessVitalsModule');
delete from permission where permissionKey in ('AccessVitalsModules', 'EditPatientVitals', 'ReadVitals', 'WriteVitals');

insert into role_permission (roleKey, permissionKey)
values ('Administrator', 'ViewPatientVitals');