--liquibase formatted sql

--changeset ivan.vakhrushev:2020.01.01:order_item.sequence
create sequence if not exists demo.order_item_seq;

--changeset ivan.vakhrushev:2020.01.01:order_item.table
create table if not exists demo.order_item
(
    id           bigint primary key      default nextval('demo.order_item_seq'),
    order_id     bigint         not null references demo.orders (id),
    price        decimal(22, 2) not null default 0,
    amount       int            not null default 0,
    sku          varchar(255)   not null,
    warehouse_id int            not null,
    unique (sku, order_id)
);

--changeset ivan.vakhrushev:2020.01.01:order_item.unique_index
create unique index if not exists i_order_item_sku_order_id_unique on demo.order_item (sku, order_id);
