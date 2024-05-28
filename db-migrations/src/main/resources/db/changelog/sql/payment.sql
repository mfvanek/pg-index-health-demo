--liquibase formatted sql

--changeset ivan.vakhrushev:2020.01.02:payment.table
create table if not exists demo.payment
(
    id            bigint         not null,
    order_id      bigint references demo.orders (id),
    status        int            not null,
    created_at    timestamp      not null,
    payment_total decimal(22, 2) not null,
    unique (id)
);

--changeset ivan.vakhrushev:2022.05.27:payment.adding.jsonb.column
alter table if exists demo.payment add column info jsonb;

--changeset ivan.vakhrushev:2024.05.28:payment.adding.autoinc.column
create sequence if not exists demo.payment_num_seq as smallint;
alter table if exists demo.payment add column num smallint not null default nextval('demo.payment_num_seq');

--changeset ivan.vakhrushev:2020.01.02:payment.populate.data
insert into demo.payment
select data.id, null, 1, now(), 1.1, '{ " payment": { "result": "success", "date": "2022-05-27T18:31:42" } }'
from generate_series(1, 30000) as data(id);

--changeset ivan.vakhrushev:2022.07.10:payment.comments.on.table.and.columns
comment on table demo.payment is 'Information about the buyer''s payments';
comment on column demo.payment.id is 'Unique identifier of the record in the current table';
comment on column demo.payment.order_id is 'Identifier of the buyer''s order';
comment on column demo.payment.status is 'Payment status';
comment on column demo.payment.created_at is 'Date and time of payment creation';
comment on column demo.payment.payment_total is 'Payment amount in the buyer''s currency';
comment on column demo.payment.info is 'Raw payment data';
comment on column demo.payment.num is 'Payment number';
