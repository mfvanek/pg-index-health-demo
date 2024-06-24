--liquibase formatted sql

--changeset vadim.khizhin:2024.06.24:courier.table
create table if not exists demo.courier (
    id bigserial primary key,
    first_name  varchar(255) not null,
    last_name   varchar(255) not null,
    phone       varchar(20)  not null,
    email       varchar(50)  not null
);

comment on table demo.courier is 'Information about couriers that deliver orders';
comment on column demo.courier.id is 'Unique identifier of the record in the current table';
comment on column demo.courier.first_name is 'Courier''s given name';
comment on column demo.courier.last_name is 'Courier''s last name';
comment on column demo.courier.phone is 'Courier''s phone number';
comment on column demo.courier.email is 'Courier''s email address';
