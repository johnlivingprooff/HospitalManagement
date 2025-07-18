 drop view if exists active_appointments_v;
 drop view if exists cancelled_appointments_v;
 drop view if exists completed_appointments_v;
 drop view if exists appointments_v;

alter table appointments add column schedule bigint references schedules (id);

create view appointments_v
as
 SELECT a.*,
    p.mrn AS patientmrn,
    (p.firstname || ' '::text) || p.lastname AS patientname,
    (acc1.firstname || ' '::text) || acc1.lastname AS doctorname,
    (acc2.firstname || ' '::text) || acc2.lastname AS receptionistname,
    dept.name AS department,
    (a.cancelled IS FALSE AND a.completed IS FALSE) AS active
   FROM appointments a
     JOIN patient p ON p.id = a.patient_id
     JOIN account acc1 ON acc1.id = a.doctor_id
     JOIN account acc2 ON acc2.id = a.created_by
     JOIN department dept ON dept.id = acc1.departmentId;

create view active_appointments_v
as
 SELECT *
   FROM appointments_v
  WHERE active = true;

create view cancelled_appointments_v
as
 SELECT *
   FROM appointments_v
  WHERE cancelled = true;

create view completed_appointments_v
as
 SELECT *
   FROM appointments_v
  WHERE completed = true;