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

--changeset ivan.vakhrushev:2020.01.02:payment.populate.data
insert into demo.payment
select data.id, null, 1, now(), 1.1
from generate_series(1, 30000) as data(id)
