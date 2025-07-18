alter table patient
    add column idNumber text not null default '',
    add column idType text not null default 'Passport',
    add column idExpiration date not null default '2021-01-01';

update patient set idNumber = mrn;

ALTER TABLE patient ADD UNIQUE (idNumber);