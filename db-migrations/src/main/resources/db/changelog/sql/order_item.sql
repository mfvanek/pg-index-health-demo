--liquibase formatted sql

--changeset ivan.vakhrushev:2020.01.01:order_item.sequence
create sequence if not exists demo.order_item_seq;

--changeset ivan.vakhrushev:2020.01.01:order_item.table
create table if not exists demo.order_item
(
    id bigint primary key default nextval('demo.order_item_seq'),
    order_id bigint not null references demo.orders (id),
    price decimal(22, 2) not null default 0,
    amount int not null default 0,
    sku varchar(255) not null,
    warehouse_id int not null,
    unique (sku, order_id)
);

--changeset ivan.vakhrushev:2020.01.01:order_item.unique_index
create unique index if not exists i_order_item_sku_order_id_unique on demo.order_item (sku, order_id);

--changeset ivan.vakhrushev:2022.07.10:order_item.comments.on.table.and.columns
comment on table demo.order_item is 'Information about the items in the order';
comment on column demo.order_item.id is 'Unique identifier of the record in the current table';
comment on column demo.order_item.order_id is 'Identifier of the buyer''s order';
comment on column demo.order_item.price is 'The cost of a unit of goods';
comment on column demo.order_item.amount is 'The number of units of the product in the position';
comment on column demo.order_item.sku is 'Stock keeping unit ';
comment on column demo.order_item.warehouse_id is 'Warehouse ID';

--changeset ivan.vakhrushev:2024.05.04:order_item.not_valid_constraint
alter table demo.order_item add constraint order_item_amount_less_than_100 check (amount < 100) not valid;

--changeset vadim.khizhin:2024.05.04:order_idem.add.array.column.and.index
alter table demo.order_item add column categories text[];
comment on column demo.order_item.categories is 'Categories to which the order item belongs';
create index if not exists order_item_categories_idx on demo.order_item (categories) where categories is not null;

--changeset ivan.vakhrushev:2020.10.10:order_item.duplicated_foreign_key
alter table demo.order_item add constraint order_item_order_id_fk_duplicate foreign key (order_id) references demo.orders (id);

--changeset ivan.vakhrushev:2024.11.01:warehouse.reference
alter table demo.order_item add constraint order_item_warehouse_id_fk foreign key (warehouse_id) references demo.warehouse (id);
