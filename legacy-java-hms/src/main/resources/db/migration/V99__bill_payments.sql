create table bill_payments (
    id bigserial primary key,
    bill_id bigint not null references bills(id),
    amount numeric (20,2) not null,
    name text,
    phone text,
    address text,
    details text not null,
    created_at timestamptz not null,
    created_by bigint not null references account(id)
);
