--liquibase formatted sql

--changeset ivan.vakhrushev:2020.01.01:buyer.sequence
create sequence if not exists demo.buyer_seq;

--changeset ivan.vakhrushev:2020.01.01:buyer.table
create table if not exists demo.buyer
(
    id          bigint primary key default nextval('demo.buyer_seq'),
    first_name  varchar(255) not null,
    last_name   varchar(255) not null,
    middle_name varchar(255),
    phone       varchar(20)  not null,
    email       varchar(50)  not null,
    ip_address  varchar(100)
);

--changeset ivan.vakhrushev:2020.01.01:buyer.indexes
create index if not exists i_buyer_first_name on demo.buyer (first_name);
create index if not exists i_buyer_last_name on demo.buyer (last_name);
create index if not exists i_buyer_middle_name on demo.buyer (middle_name);
create index if not exists i_buyer_names on demo.buyer (first_name, last_name, middle_name);

--changeset ivan.vakhrushev:2020.01.02:populate.data
insert into demo.buyer (first_name, last_name, phone, email)
values ('John', 'Smith', '89201213456', 'john@example.com'),
    ('Mary', 'Smith', '89201213457', 'hello@example.com'),
    ('Anna', 'Smith', '89201213458', 'hello@example.com');

--changeset ivan.vakhrushev:2020.01.02:create.unique.index.on.email runInTransaction:false failOnError:false
create unique index concurrently if not exists i_buyer_email on demo.buyer (email);

--changeset ivan.vakhrushev:2020.03.29:create.index.on.phone runInTransaction:false
create index concurrently if not exists i_buyer_id_phone on demo.buyer (id, phone);
create index concurrently if not exists i_buyer_id_phone_without_ip on demo.buyer (id, phone) where ip_address is null;
