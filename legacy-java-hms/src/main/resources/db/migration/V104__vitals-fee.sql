create table vitals_fee (
    fee numeric(20,2) not null,
    updated_at timestamptz not null
);

insert into vitals_fee (fee, updated_at) values (0.00, current_timestamp);