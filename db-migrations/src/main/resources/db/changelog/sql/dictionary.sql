--liquibase formatted sql

--changeset ivan.vakhrushev:2025.04.11:dictionary.table
create table if not exists demo."dictionary-to-delete"
(
);

comment on table demo."dictionary-to-delete" is 'Unused table to delete';
