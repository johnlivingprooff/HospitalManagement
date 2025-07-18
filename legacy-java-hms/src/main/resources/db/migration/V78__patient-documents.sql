create table if not exists patient_documents (
    id bigserial primary key,
    name text not null,
    hidden boolean not null,
    patient_id bigint not null references patient(id) match full,
    attachment text not null,
    created timestamptz not null default current_timestamp
);