drop view schedules_v;

create view schedules_v
as
SELECT s.id,
	s.doctor_id,
	s.start_date as starts,
	s.end_date as ends,
	(a.firstname || ' ' || a.lastname) AS doctorName,
	s.end_date < current_timestamp expired
FROM schedules s
JOIN account a ON a.id = s.doctor_id;


create function addSingleScheduleDay(in doctorId bigint, in _start_date_ timestamptz, in _end_date_ timestamptz)
returns void
language plpgsql
as
$$
begin
insert into schedules (doctor_id, start_date, end_date) values ($1, $2, $3);
end;
$$;


create function addMultipleScheduleDays(in doctorId bigint, in _start_date_ date, in _end_date_ date, in _start_time time, in _end_time time)
returns void
language plpgsql
as
$$
begin
with dates as (
	select generate_series($2, $3, interval '1d') as start
)
insert into schedules (doctor_id, start_date, end_date)
select $1, dates.start + $4, dates.start + ($5 - $4)
from dates;
end;
$$;
