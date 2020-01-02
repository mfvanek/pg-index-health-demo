--liquibase formatted sql

--changeset ivan.vakhrushev:2020.01.01:create.schema
create schema if not exists demo;
