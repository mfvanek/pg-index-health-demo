--liquibase formatted sql

--changeset ivan.vakhrushev:2020.01.01:orders.sequence
create sequence if not exists demo.orders_seq;

--changeset ivan.vakhrushev:2020.01.01:orders.table
create table if not exists demo.orders
(
    id          bigint primary key default nextval('demo.orders_seq'),
    user_id     bigint         not null,
    shop_id     bigint         not null,
    status      int            not null,
    created_at  timestamp      not null,
    order_total decimal(22, 2) not null,
    buyer_id    bigint         not null references demo.buyer (id)
);

--changeset ivan.vakhrushev:2022.07.10:orders.comments.on.table.and.columns
comment on table demo.orders is 'Information about the buyer''s order';
comment on column demo.orders.id is 'Unique identifier of the record in the current table';
comment on column demo.orders.user_id is 'User ID';
comment on column demo.orders.shop_id is 'Seller''s ID';
comment on column demo.orders.status is 'Order status';
comment on column demo.orders.created_at is 'Date and time of order creation';
comment on column demo.orders.order_total is 'Total cost of the order';
comment on column demo.orders.buyer_id is 'Buyer''s ID';

--changeset ivan.vakhrushev:2024.05.04:orders.preorder.column
alter table demo.orders add column preorder boolean not null default false;
comment on column demo.orders.preorder is 'Is it preorder';

--changeset ivan.vakhrushev:2024.05.04:create.index.on.preorder runInTransaction:false
create index concurrently if not exists i_orders_preorder on demo.orders (preorder);
