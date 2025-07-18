alter table beds
    add column deleted boolean not null default false,
    -- If the bed is deleted, save the old unique code into this column so that a new bed maye later be added with
    -- the same unique code. The old code may then still be reference if need be.
    add column old_code text;

create view beds_v
as
select
    b.*, w.name as ward
from beds b
join wards w on w.id = b.ward_id
where b.deleted = false;

create function deleteBed(in bed_id bigint)
    returns bigint
    language plpgsql
as $$
begin
    update
        beds
    set old_code = code,
        code = ('__#deleted@' || to_hex(id)),
        deleted = true
    where
        vacant = true and id = $1;
    return 1;
end;
$$;