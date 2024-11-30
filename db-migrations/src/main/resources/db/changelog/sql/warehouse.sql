--liquibase formatted sql

--changeset ivan.vakhrushev:2024.11.01:warehouse.table
create table if not exists demo.warehouse
(
    id bigint primary key generated always as identity,
    name varchar(255) not null
);

comment on table demo.warehouse is 'Information about the warehouses';
comment on column demo.warehouse.id is 'Unique identifier of the warehouse';
comment on column demo.warehouse.name is 'Human readable name of the warehouse';
