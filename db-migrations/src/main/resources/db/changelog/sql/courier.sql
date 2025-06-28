--liquibase formatted sql

--changeset vadim.khizhin:2024.06.24:courier.table
create table if not exists demo.courier (
    id bigserial primary key,
    first_name varchar(255) not null,
    last_name varchar(255) not null,
    phone varchar(20) not null,
    email varchar(50) not null
);

comment on table demo.courier is 'Information about couriers that deliver orders';
comment on column demo.courier.id is 'Unique identifier of the record in the current table';
comment on column demo.courier.first_name is 'Courier''s given name';
comment on column demo.courier.last_name is 'Courier''s last name';
comment on column demo.courier.phone is 'Courier''s phone number';
comment on column demo.courier.email is 'Courier''s email address';

--changeset ivan.vakhrushev:2024.10.20:courier.indexes runInTransaction:false
create unique index concurrently if not exists idx_courier_phone_and_email_should_be_unique_very_long_name_that_will_be_truncated
on demo.courier (phone, email);

--changeset ivan.vakhrushev:2025.06.28:courier.created_at.column
alter table if exists demo.courier add column created_at timestamptz not null default now();
comment on column demo.courier.created_at is 'Date and time in UTC when the row was created';
