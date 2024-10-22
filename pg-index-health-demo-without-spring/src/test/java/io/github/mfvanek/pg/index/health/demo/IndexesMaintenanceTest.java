/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo;

import io.github.mfvanek.pg.checks.host.BtreeIndexesOnArrayColumnsCheckOnHost;
import io.github.mfvanek.pg.checks.host.ColumnsWithJsonTypeCheckOnHost;
import io.github.mfvanek.pg.checks.host.ColumnsWithSerialTypesCheckOnHost;
import io.github.mfvanek.pg.checks.host.ColumnsWithoutDescriptionCheckOnHost;
import io.github.mfvanek.pg.checks.host.DuplicatedForeignKeysCheckOnHost;
import io.github.mfvanek.pg.checks.host.DuplicatedIndexesCheckOnHost;
import io.github.mfvanek.pg.checks.host.ForeignKeysNotCoveredWithIndexCheckOnHost;
import io.github.mfvanek.pg.checks.host.FunctionsWithoutDescriptionCheckOnHost;
import io.github.mfvanek.pg.checks.host.IndexesWithBooleanCheckOnHost;
import io.github.mfvanek.pg.checks.host.IndexesWithNullValuesCheckOnHost;
import io.github.mfvanek.pg.checks.host.IntersectedForeignKeysCheckOnHost;
import io.github.mfvanek.pg.checks.host.IntersectedIndexesCheckOnHost;
import io.github.mfvanek.pg.checks.host.InvalidIndexesCheckOnHost;
import io.github.mfvanek.pg.checks.host.NotValidConstraintsCheckOnHost;
import io.github.mfvanek.pg.checks.host.PossibleObjectNameOverflowCheckOnHost;
import io.github.mfvanek.pg.checks.host.PrimaryKeysWithSerialTypesCheckOnHost;
import io.github.mfvanek.pg.checks.host.SequenceOverflowCheckOnHost;
import io.github.mfvanek.pg.checks.host.TablesNotLinkedToOthersCheckOnHost;
import io.github.mfvanek.pg.checks.host.TablesWithoutDescriptionCheckOnHost;
import io.github.mfvanek.pg.checks.host.TablesWithoutPrimaryKeyCheckOnHost;
import io.github.mfvanek.pg.common.maintenance.CheckTypeAware;
import io.github.mfvanek.pg.common.maintenance.DatabaseCheckOnHost;
import io.github.mfvanek.pg.common.maintenance.Diagnostic;
import io.github.mfvanek.pg.connection.PgConnection;
import io.github.mfvanek.pg.connection.PgConnectionImpl;
import io.github.mfvanek.pg.index.health.demo.support.DatabaseAwareTestBase;
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
import io.github.mfvanek.pg.model.object.AnyObject;
import io.github.mfvanek.pg.model.object.PgObjectType;
import io.github.mfvanek.pg.model.sequence.SequenceState;
import io.github.mfvanek.pg.model.table.Table;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

@SuppressWarnings({"checkstyle:ClassDataAbstractionCoupling", "checkstyle:ClassFanOutComplexity"})
class IndexesMaintenanceTest extends DatabaseAwareTestBase {

    private static final int SKIPPED_CHECKS_COUNT = 4; // indexes with bloat, tables with bloat, unused indexes, tables with missing indexes
    private static final String BUYER_TABLE = "demo.buyer";
    private static final String ORDER_ITEM_TABLE = "demo.order_item";
    private static final String ORDERS_TABLE = "demo.orders";
    private static final String ORDER_ID_COLUMN = "order_id";

    private final PgContext demoSchema = PgContext.of("demo");
    private final List<DatabaseCheckOnHost<? extends DbObject>> checks;

    IndexesMaintenanceTest() {
        final PgConnection pgConnection = PgConnectionImpl.of(getDataSource(), getHost());
        this.checks = List.of(
            new InvalidIndexesCheckOnHost(pgConnection),
            new DuplicatedIndexesCheckOnHost(pgConnection),
            new IntersectedIndexesCheckOnHost(pgConnection),
            new ForeignKeysNotCoveredWithIndexCheckOnHost(pgConnection),
            new TablesWithoutPrimaryKeyCheckOnHost(pgConnection),
            new IndexesWithNullValuesCheckOnHost(pgConnection),
            new TablesWithoutDescriptionCheckOnHost(pgConnection),
            new ColumnsWithoutDescriptionCheckOnHost(pgConnection),
            new ColumnsWithJsonTypeCheckOnHost(pgConnection),
            new ColumnsWithSerialTypesCheckOnHost(pgConnection),
            new FunctionsWithoutDescriptionCheckOnHost(pgConnection),
            new IndexesWithBooleanCheckOnHost(pgConnection),
            new NotValidConstraintsCheckOnHost(pgConnection),
            new BtreeIndexesOnArrayColumnsCheckOnHost(pgConnection),
            new SequenceOverflowCheckOnHost(pgConnection),
            new PrimaryKeysWithSerialTypesCheckOnHost(pgConnection),
            new DuplicatedForeignKeysCheckOnHost(pgConnection),
            new IntersectedForeignKeysCheckOnHost(pgConnection),
            new PossibleObjectNameOverflowCheckOnHost(pgConnection),
            new TablesNotLinkedToOthersCheckOnHost(pgConnection)
        );
    }

    @Test
    @DisplayName("Always check PostgreSQL version in your tests")
    void checkPostgresVersion() throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("select version();")) {
                resultSet.next();
                final String pgVersion = resultSet.getString(1);
                assertThat(pgVersion).startsWith("PostgreSQL 16.4");
            }
        }
    }

    @Test
    void completenessTest() {
        assertThat(checks)
            .hasSize(Diagnostic.values().length - SKIPPED_CHECKS_COUNT)
            .filteredOn(c -> c.getDiagnostic() != Diagnostic.SEQUENCE_OVERFLOW)
            .as("Only static checks should present in list")
            .allMatch(CheckTypeAware::isStatic);
    }

    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    @Test
    void databaseStructureCheckForPublicSchema() {
        checks.forEach(check -> {
            final List<? extends DbObject> checkResult = check.check();
            switch (check.getDiagnostic()) {
                case TABLES_WITHOUT_PRIMARY_KEY, TABLES_WITHOUT_DESCRIPTION, TABLES_NOT_LINKED_TO_OTHERS -> assertThat(checkResult)
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
            .hasSize(Diagnostic.values().length - SKIPPED_CHECKS_COUNT);

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

                case DUPLICATED_FOREIGN_KEYS -> assertThat(checkResult)
                    .asInstanceOf(list(DuplicatedForeignKeys.class))
                    .hasSize(1)
                    .containsExactly(DuplicatedForeignKeys.of(
                        ForeignKey.ofNotNullColumn(ORDER_ITEM_TABLE, "order_item_order_id_fk_duplicate", ORDER_ID_COLUMN),
                        ForeignKey.ofNotNullColumn(ORDER_ITEM_TABLE, "order_item_order_id_fkey", ORDER_ID_COLUMN))
                    );

                case POSSIBLE_OBJECT_NAME_OVERFLOW -> assertThat(checkResult)
                    .asInstanceOf(list(AnyObject.class))
                    .hasSize(1)
                    .containsExactly(
                        AnyObject.ofType("demo.idx_courier_phone_and_email_should_be_unique_very_long_name_tha", PgObjectType.INDEX));

                default -> assertThat(checkResult).isEmpty();
            }
        });
    }
}
