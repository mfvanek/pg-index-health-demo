--liquibase formatted sql

--changeset ivan.vakhrushev:2020.01.01:orders.sequence
create sequence if not exists orders_seq;

--changeset ivan.vakhrushev:2020.01.01:orders.table
create table if not exists orders
(
    id          bigint primary key default nextval('orders_seq'),
    user_id     bigint         not null,
    shop_id     bigint         not null,
    status      int            not null,
    created_at  timestamp      not null,
    order_total decimal(22, 2) not null,
    buyer_id    bigint         not null references buyer (id)
);
