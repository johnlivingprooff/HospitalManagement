alter table patient_vitals
    drop column vital,
    drop column value,
    drop column modified,
    add column pulse int not null,
    add column breaths int not null,
    add column temperature numeric (4,2) not null,
    add column systolic int not null,
    add column diastolic int not null;