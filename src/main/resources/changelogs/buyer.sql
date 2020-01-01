--liquibase formatted sql

--changeset ivan.vakhrushev:2020.01.01:buyer.sequence
create sequence if not exists buyer_seq;

--changeset ivan.vakhrushev:2020.01.01:buyer.table
create table if not exists buyer
(
    id          bigint primary key default nextval('buyer_seq'),
    first_name  varchar(255) not null,
    last_name   varchar(255) not null,
    middle_name varchar(255),
    phone       varchar(20)  not null,
    email       varchar(50)  not null,
    ip_address  varchar(100),
    unique (email)
);

--changeset ivan.vakhrushev:2020.01.01:buyer.indexes
create index if not exists i_buyer_first_name on buyer (first_name);
create index if not exists i_buyer_last_name on buyer (last_name);
create index if not exists i_buyer_middle_name on buyer (middle_name);
create index if not exists i_buyer_names on buyer (first_name, last_name, middle_name);
