--liquibase formatted sql

--changeset ivan.vakhrushev:2025.04.11:dictionary.table
-- noqa: disable=RF05
create table if not exists demo."dictionary-to-delete"
(
    "dict-id" bigint not null generated always as identity
);

comment on table demo."dictionary-to-delete" is 'Unused table to delete';
