create table if not exists bills (
    id bigserial primary key,
    balance numeric not null,
    paid numeric not null,
    patient_id bigint not null references patient (id) match full,
    status text not null,
    bill_type text not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table if not exists prescription_bills (
    bill_id bigint primary key references bills(id) match full,
    prescription_id bigint references prescriptions (id) match full
);