create table procedures (
    id bigserial primary key,
    name text not null,
    cost numeric (17,2) not null,
    procedure_type text not null,
    deleted boolean not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create view procedures_v
as
select *
from procedures
where deleted = false;