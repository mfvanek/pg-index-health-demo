/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo;

import io.github.mfvanek.pg.common.maintenance.DatabaseCheckOnHost;
import io.github.mfvanek.pg.common.maintenance.Diagnostic;
import io.github.mfvanek.pg.index.health.demo.utils.BasePgIndexHealthDemoSpringBootTest;
import io.github.mfvanek.pg.model.DbObject;
import io.github.mfvanek.pg.model.PgContext;
import io.github.mfvanek.pg.model.column.Column;
import io.github.mfvanek.pg.model.column.ColumnWithSerialType;
import io.github.mfvanek.pg.model.constraint.Constraint;
import io.github.mfvanek.pg.model.constraint.ConstraintType;
import io.github.mfvanek.pg.model.constraint.DuplicatedForeignKeys;
import io.github.mfvanek.pg.model.constraint.ForeignKey;
import io.github.mfvanek.pg.model.index.DuplicatedIndexes;
import io.github.mfvanek.pg.model.index.Index;
import io.github.mfvanek.pg.model.index.IndexWithColumns;
import io.github.mfvanek.pg.model.index.IndexWithNulls;
import io.github.mfvanek.pg.model.index.IndexWithSize;
import io.github.mfvanek.pg.model.sequence.SequenceState;
import io.github.mfvanek.pg.model.table.Table;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

class IndexesMaintenanceTest extends BasePgIndexHealthDemoSpringBootTest {

    private static final String BUYER_TABLE = "demo.buyer";
    private static final String ORDER_ITEM_TABLE = "demo.order_item";
    private static final String ORDERS_TABLE = "demo.orders";
    private static final String ORDER_ID_COLUMN = "order_id";

    private final PgContext demoSchema = PgContext.of("demo");

    @Autowired
    private List<DatabaseCheckOnHost<? extends DbObject>> checks;

    @Test
    @DisplayName("Always check PostgreSQL version in your tests")
    void checkPostgresVersion() {
        final String pgVersion = jdbcTemplate.queryForObject("select version();", String.class);
        assertThat(pgVersion)
            .startsWith("PostgreSQL 16.4");
    }

    @Test
    void databaseStructureCheckForPublicSchema() {
        assertThat(checks)
            .hasSize(Diagnostic.values().length);

        checks.forEach(check -> {
            final List<? extends DbObject> checkResult = check.check();
            switch (check.getDiagnostic()) {
                case TABLES_WITHOUT_PRIMARY_KEY, TABLES_WITHOUT_DESCRIPTION -> assertThat(checkResult)
                    .asInstanceOf(list(Table.class))
                    .hasSize(1)
                    // HOW TO FIX: just add liquibase table to exclusions
                    .containsExactly(Table.of("databasechangelog", 0L));

                case COLUMNS_WITHOUT_DESCRIPTION -> assertThat(checkResult)
                    .asInstanceOf(list(Column.class))
                    // HOW TO FIX: just add liquibase table to exclusions
                    .hasSize(14)
                    .allMatch(c -> "databasechangelog".equals(c.getTableName()));

                default -> assertThat(checkResult).isEmpty();
            }
        });
    }

    @SuppressWarnings({"checkstyle:CyclomaticComplexity", "checkstyle:LambdaBodyLength"})
    @Test
    void databaseStructureCheckForDemoSchema() {
        assertThat(checks)
            .hasSize(Diagnostic.values().length);

        checks.forEach(check -> {
            final List<? extends DbObject> checkResult = check.check(demoSchema);
            switch (check.getDiagnostic()) {
                case INVALID_INDEXES -> assertThat(checkResult)
                    .asInstanceOf(list(Index.class))
                    .hasSize(1)
                    // HOW TO FIX: drop index concurrently, fix data in table, then create index concurrently again
                    .containsExactly(Index.of(BUYER_TABLE, "demo.i_buyer_email"));

                case DUPLICATED_INDEXES -> assertThat(checkResult)
                    .asInstanceOf(list(DuplicatedIndexes.class))
                    .hasSize(1)
                    // HOW TO FIX: do not manually create index for column with unique constraint
                    .containsExactly(DuplicatedIndexes.of(
                        IndexWithSize.of(ORDER_ITEM_TABLE, "demo.i_order_item_sku_order_id_unique", 8_192L),
                        IndexWithSize.of(ORDER_ITEM_TABLE, "demo.order_item_sku_order_id_key", 8_192L)));

                case INTERSECTED_INDEXES -> assertThat(checkResult)
                    .asInstanceOf(list(DuplicatedIndexes.class))
                    .hasSize(2)
                    // HOW TO FIX: consider using an index with a different column order or just delete unnecessary indexes
                    .containsExactlyInAnyOrder(
                        DuplicatedIndexes.of(
                            IndexWithSize.of(BUYER_TABLE, "demo.buyer_pkey", 1L),
                            IndexWithSize.of(BUYER_TABLE, "demo.i_buyer_id_phone", 1L)),
                        DuplicatedIndexes.of(
                            IndexWithSize.of(BUYER_TABLE, "demo.i_buyer_first_name", 1L),
                            IndexWithSize.of(BUYER_TABLE, "demo.i_buyer_names", 1L)));

                case FOREIGN_KEYS_WITHOUT_INDEX -> assertThat(checkResult)
                    .asInstanceOf(list(ForeignKey.class))
                    .hasSize(4)
                    // HOW TO FIX: create indexes on columns under foreign key constraint
                    .containsExactlyInAnyOrder(
                        ForeignKey.ofNotNullColumn(ORDER_ITEM_TABLE, "order_item_order_id_fkey", ORDER_ID_COLUMN),
                        ForeignKey.ofNotNullColumn(ORDER_ITEM_TABLE, "order_item_order_id_fk_duplicate", ORDER_ID_COLUMN),
                        ForeignKey.ofNotNullColumn(ORDERS_TABLE, "orders_buyer_id_fkey", "buyer_id"),
                        ForeignKey.ofNullableColumn("demo.payment", "payment_order_id_fkey", ORDER_ID_COLUMN));

                case TABLES_WITHOUT_PRIMARY_KEY -> assertThat(checkResult)
                    .asInstanceOf(list(Table.class))
                    .hasSize(1)
                    // HOW TO FIX: add primary key to the table
                    .containsExactly(Table.of("demo.payment", 1L));

                case INDEXES_WITH_NULL_VALUES -> assertThat(checkResult)
                    .asInstanceOf(list(IndexWithNulls.class))
                    .hasSize(1)
                    // HOW TO FIX: consider excluding null values from index if it's possible
                    .containsExactly(IndexWithNulls.of(BUYER_TABLE, "demo.i_buyer_middle_name", 1L, "middle_name"));

                case INDEXES_WITH_BOOLEAN -> assertThat(checkResult)
                    .asInstanceOf(list(IndexWithColumns.class))
                    .hasSize(1)
                    .contains(IndexWithColumns.ofSingle(ORDERS_TABLE, "demo.i_orders_preorder", 1L,
                        Column.ofNotNull(ORDERS_TABLE, "preorder")));

                case NOT_VALID_CONSTRAINTS -> assertThat(checkResult)
                    .asInstanceOf(list(Constraint.class))
                    .hasSize(1)
                    .contains(Constraint.ofType(ORDER_ITEM_TABLE, "order_item_amount_less_than_100", ConstraintType.CHECK));

                case BTREE_INDEXES_ON_ARRAY_COLUMNS -> assertThat(checkResult)
                    .asInstanceOf(list(IndexWithColumns.class))
                    .hasSize(1)
                    .containsExactly(IndexWithColumns.ofSingle(ORDER_ITEM_TABLE, "demo.order_item_categories_idx", 8_192L,
                        Column.ofNullable(ORDER_ITEM_TABLE, "categories")));

                case SEQUENCE_OVERFLOW -> assertThat(checkResult)
                    .asInstanceOf(list(SequenceState.class))
                    .hasSize(1)
                    .containsExactly(SequenceState.of("demo.payment_num_seq", "smallint", 8.44));

                case PRIMARY_KEYS_WITH_SERIAL_TYPES -> assertThat(checkResult)
                    .asInstanceOf(list(ColumnWithSerialType.class))
                    .hasSize(1)
                    .containsExactly(ColumnWithSerialType.ofBigSerial(Column.ofNotNull("demo.courier", "id"), "demo.courier_id_seq"));

                case UNUSED_INDEXES -> assertThat(checkResult).isNotEmpty(); // TODO Just ignore this "runtime" check https://github.com/mfvanek/pg-index-health/issues/456

                case DUPLICATED_FOREIGN_KEYS -> assertThat(checkResult)
                    .asInstanceOf(list(DuplicatedForeignKeys.class))
                    .hasSize(1)
                    .containsExactly(DuplicatedForeignKeys.of(
                        ForeignKey.ofNotNullColumn(ORDER_ITEM_TABLE, "order_item_order_id_fk_duplicate", ORDER_ID_COLUMN),
                        ForeignKey.ofNotNullColumn(ORDER_ITEM_TABLE, "order_item_order_id_fkey", ORDER_ID_COLUMN))
                    );

                default -> assertThat(checkResult).isEmpty();
            }
        });
    }
}
