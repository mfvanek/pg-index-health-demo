--liquibase formatted sql

--changeset ivan.vakhrushev:2025.06.29:reports.create.table
create table if not exists demo.reports
(
    report_date date not null,
    shop_id bigint not null,
    created_at timestamptz not null,
    orders_count bigint not null,
    orders_total numeric(22, 2) not null,
    primary key (report_date, shop_id)
) partition by range (report_date);

comment on table demo.reports is 'Daily aggregated report of shop orders, partitioned by report_date.';

comment on column demo.reports.report_date is 'Date of the report (partition key). Each partition stores data for one date or range of dates.';
comment on column demo.reports.shop_id is 'Unique identifier of the shop.';
comment on column demo.reports.created_at is 'Timestamp in UTC when the report entry was created.';
comment on column demo.reports.orders_count is 'Total number of orders made by the shop on the report date.';
comment on column demo.reports.orders_total is 'Total value of orders made by the shop on the report date.';

--changeset ivan.vakhrushev:2025.06.29:reports.partitions.create
create table if not exists demo.reports_2024 partition of demo.reports
for values from (to_date('2024-01-01', 'YYYY-MM-DD')) to (to_date('2025-01-01', 'YYYY-MM-DD'));

create table if not exists demo.reports_2025 partition of demo.reports
for values from (to_date('2025-01-01', 'YYYY-MM-DD')) to (to_date('2026-01-01', 'YYYY-MM-DD'));

create table demo.reports_2026 partition of demo.reports
for values from (to_date('2026-01-01', 'YYYY-MM-DD')) to (to_date('2027-01-01', 'YYYY-MM-DD'));

comment on table demo.reports_2024 is 'Partition of reports for report_date from 2024-01-01 (inclusive) to 2025-01-01 (exclusive).';
comment on table demo.reports_2025 is 'Partition of reports for report_date from 2025-01-01 (inclusive) to 2026-01-01 (exclusive).';
comment on table demo.reports_2026 is 'Partition of reports for report_date from 2026-01-01 (inclusive) to 2027-01-01 (exclusive).';

--changeset ivan.vakhrushev:2025.06.29:reports.generate.data
insert into demo.reports (report_date, shop_id, created_at, orders_count, orders_total) values
('2024-01-10', 1, now(), 10, 100.00),
('2024-03-15', 2, now(), 5, 250.50),
('2024-05-01', 3, now(), 8, 145.75),
('2024-08-20', 4, now(), 12, 199.99),
('2024-12-31', 5, now(), 3, 80.00);

insert into demo.reports (report_date, shop_id, created_at, orders_count, orders_total) values
('2025-01-05', 1, now(), 11, 110.00),
('2025-04-10', 2, now(), 9, 210.00),
('2025-06-22', 3, now(), 6, 99.99),
('2025-09-15', 4, now(), 7, 160.00),
('2025-12-25', 5, now(), 2, 60.00);
