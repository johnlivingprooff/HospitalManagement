drop function addMultipleScheduleDays, addSingleScheduleDay;

create function addSingleScheduleDay(in doctorId bigint, in _start_date_ timestamptz, in _end_date_ timestamptz)
returns bigint
language plpgsql
as
$$
begin
insert into schedules (doctor_id, start_date, end_date) values ($1, $2, $3);
return 0;
end;
$$;


create function addMultipleScheduleDays(in doctorId bigint, in _start_date_ date, in _end_date_ date, in _start_time time, in _end_time time)
returns bigint
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
return 0;
end;
$$;
