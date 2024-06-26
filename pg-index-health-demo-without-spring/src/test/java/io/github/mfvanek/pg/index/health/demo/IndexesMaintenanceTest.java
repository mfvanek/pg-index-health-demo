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
import io.github.mfvanek.pg.checks.host.DuplicatedIndexesCheckOnHost;
import io.github.mfvanek.pg.checks.host.ForeignKeysNotCoveredWithIndexCheckOnHost;
import io.github.mfvanek.pg.checks.host.FunctionsWithoutDescriptionCheckOnHost;
import io.github.mfvanek.pg.checks.host.IndexesWithBooleanCheckOnHost;
import io.github.mfvanek.pg.checks.host.IndexesWithNullValuesCheckOnHost;
import io.github.mfvanek.pg.checks.host.IntersectedIndexesCheckOnHost;
import io.github.mfvanek.pg.checks.host.InvalidIndexesCheckOnHost;
import io.github.mfvanek.pg.checks.host.NotValidConstraintsCheckOnHost;
import io.github.mfvanek.pg.checks.host.PrimaryKeysWithSerialTypesCheckOnHost;
import io.github.mfvanek.pg.checks.host.SequenceOverflowCheckOnHost;
import io.github.mfvanek.pg.checks.host.TablesWithoutDescriptionCheckOnHost;
import io.github.mfvanek.pg.checks.host.TablesWithoutPrimaryKeyCheckOnHost;
import io.github.mfvanek.pg.connection.PgConnection;
import io.github.mfvanek.pg.connection.PgConnectionImpl;
import io.github.mfvanek.pg.index.health.demo.support.DatabaseAwareTestBase;
import io.github.mfvanek.pg.model.PgContext;
import io.github.mfvanek.pg.model.column.Column;
import io.github.mfvanek.pg.model.column.ColumnWithSerialType;
import io.github.mfvanek.pg.model.constraint.Constraint;
import io.github.mfvanek.pg.model.constraint.ConstraintType;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"checkstyle:ClassDataAbstractionCoupling", "checkstyle:ClassFanOutComplexity", "PMD.AvoidDuplicateLiterals"})
class IndexesMaintenanceTest extends DatabaseAwareTestBase {

    private static final String BUYER_TABLE = "demo.buyer";
    private static final String ORDER_ITEM_TABLE = "demo.order_item";
    private static final String ORDERS_TABLE = "demo.orders";

    private final PgContext demoSchema = PgContext.of("demo");
    private final InvalidIndexesCheckOnHost invalidIndexesCheck;
    private final DuplicatedIndexesCheckOnHost duplicatedIndexesCheck;
    private final IntersectedIndexesCheckOnHost intersectedIndexesCheck;
    private final ForeignKeysNotCoveredWithIndexCheckOnHost foreignKeysNotCoveredWithIndexCheck;
    private final TablesWithoutPrimaryKeyCheckOnHost tablesWithoutPrimaryKeyCheck;
    private final IndexesWithNullValuesCheckOnHost indexesWithNullValuesCheck;
    private final TablesWithoutDescriptionCheckOnHost tablesWithoutDescriptionCheck;
    private final ColumnsWithoutDescriptionCheckOnHost columnsWithoutDescriptionCheck;
    private final ColumnsWithJsonTypeCheckOnHost columnsWithJsonTypeCheck;
    private final ColumnsWithSerialTypesCheckOnHost columnsWithSerialTypesCheck;
    private final FunctionsWithoutDescriptionCheckOnHost functionsWithoutDescriptionCheck;
    private final IndexesWithBooleanCheckOnHost indexesWithBooleanCheck;
    private final NotValidConstraintsCheckOnHost notValidConstraintsCheck;
    private final BtreeIndexesOnArrayColumnsCheckOnHost btreeIndexesOnArrayColumnsCheck;
    private final SequenceOverflowCheckOnHost sequenceOverflowCheck;
    private final PrimaryKeysWithSerialTypesCheckOnHost primaryKeysWithSerialTypesCheck;

    IndexesMaintenanceTest() {
        final PgConnection pgConnection = PgConnectionImpl.of(getDataSource(), getHost());
        this.invalidIndexesCheck = new InvalidIndexesCheckOnHost(pgConnection);
        this.duplicatedIndexesCheck = new DuplicatedIndexesCheckOnHost(pgConnection);
        this.intersectedIndexesCheck = new IntersectedIndexesCheckOnHost(pgConnection);
        this.foreignKeysNotCoveredWithIndexCheck = new ForeignKeysNotCoveredWithIndexCheckOnHost(pgConnection);
        this.tablesWithoutPrimaryKeyCheck = new TablesWithoutPrimaryKeyCheckOnHost(pgConnection);
        this.indexesWithNullValuesCheck = new IndexesWithNullValuesCheckOnHost(pgConnection);
        this.tablesWithoutDescriptionCheck = new TablesWithoutDescriptionCheckOnHost(pgConnection);
        this.columnsWithoutDescriptionCheck = new ColumnsWithoutDescriptionCheckOnHost(pgConnection);
        this.columnsWithJsonTypeCheck = new ColumnsWithJsonTypeCheckOnHost(pgConnection);
        this.columnsWithSerialTypesCheck = new ColumnsWithSerialTypesCheckOnHost(pgConnection);
        this.functionsWithoutDescriptionCheck = new FunctionsWithoutDescriptionCheckOnHost(pgConnection);
        this.indexesWithBooleanCheck = new IndexesWithBooleanCheckOnHost(pgConnection);
        this.notValidConstraintsCheck = new NotValidConstraintsCheckOnHost(pgConnection);
        this.btreeIndexesOnArrayColumnsCheck = new BtreeIndexesOnArrayColumnsCheckOnHost(pgConnection);
        this.sequenceOverflowCheck = new SequenceOverflowCheckOnHost(pgConnection);
        this.primaryKeysWithSerialTypesCheck = new PrimaryKeysWithSerialTypesCheckOnHost(pgConnection);
    }

    @Test
    @DisplayName("Always check PostgreSQL version in your tests")
    void checkPostgresVersion() throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("select version();")) {
                resultSet.next();
                final String pgVersion = resultSet.getString(1);
                assertThat(pgVersion).startsWith("PostgreSQL 16.2");
            }
        }
    }

    @Test
    void getInvalidIndexesShouldReturnNothingForPublicSchema() {
        assertThat(invalidIndexesCheck.check())
            .isEmpty();
    }

    @Test
    void getInvalidIndexesShouldReturnOneRowForDemoSchema() {
        assertThat(invalidIndexesCheck.check(demoSchema))
            .hasSize(1)
            // HOW TO FIX: drop index concurrently, fix data in table, then create index concurrently again
            .containsExactly(Index.of(BUYER_TABLE, "demo.i_buyer_email"));
    }

    @Test
    void getDuplicatedIndexesShouldReturnNothingForPublicSchema() {
        assertThat(duplicatedIndexesCheck.check())
            .isEmpty();
    }

    @Test
    void getDuplicatedIndexesShouldReturnOneRowForDemoSchema() {
        assertThat(duplicatedIndexesCheck.check(demoSchema))
            .hasSize(1)
            // HOW TO FIX: do not manually create index for column with unique constraint
            .containsExactly(DuplicatedIndexes.of(
                IndexWithSize.of(ORDER_ITEM_TABLE, "demo.i_order_item_sku_order_id_unique", 8_192L),
                IndexWithSize.of(ORDER_ITEM_TABLE, "demo.order_item_sku_order_id_key", 8_192L)));
    }

    @Test
    void getIntersectedIndexesShouldReturnNothingForPublicSchema() {
        assertThat(intersectedIndexesCheck.check())
            .isEmpty();
    }

    @Test
    void getIntersectedIndexesShouldReturnTwoRowsForDemoSchema() {
        assertThat(intersectedIndexesCheck.check(demoSchema))
            .hasSize(2)
            // HOW TO FIX: consider using an index with a different column order or just delete unnecessary indexes
            .containsExactlyInAnyOrder(
                DuplicatedIndexes.of(
                    IndexWithSize.of(BUYER_TABLE, "demo.buyer_pkey", 1L),
                    IndexWithSize.of(BUYER_TABLE, "demo.i_buyer_id_phone", 1L)),
                DuplicatedIndexes.of(
                    IndexWithSize.of(BUYER_TABLE, "demo.i_buyer_first_name", 1L),
                    IndexWithSize.of(BUYER_TABLE, "demo.i_buyer_names", 1L)));
    }

    @Test
    void getForeignKeysNotCoveredWithIndexShouldReturnNothingForPublicSchema() {
        assertThat(foreignKeysNotCoveredWithIndexCheck.check())
            .isEmpty();
    }

    @Test
    void getForeignKeysNotCoveredWithIndexShouldReturnThreeRowsForDemoSchema() {
        assertThat(foreignKeysNotCoveredWithIndexCheck.check(demoSchema))
            .hasSize(3)
            // HOW TO FIX: create indexes on columns under foreign key constraint
            .containsExactlyInAnyOrder(
                ForeignKey.ofNotNullColumn(ORDER_ITEM_TABLE, "order_item_order_id_fkey", "order_id"),
                ForeignKey.ofNotNullColumn(ORDERS_TABLE, "orders_buyer_id_fkey", "buyer_id"),
                ForeignKey.ofNullableColumn("demo.payment", "payment_order_id_fkey", "order_id"));
    }

    @Test
    void getTablesWithoutPrimaryKeyShouldReturnOneRowForPublicSchema() {
        assertThat(tablesWithoutPrimaryKeyCheck.check())
            .hasSize(1)
            // HOW TO FIX: just add liquibase table to exclusions
            .containsExactly(Table.of("databasechangelog", 1L));
    }

    @Test
    void getTablesWithoutPrimaryKeyShouldReturnOneRowForDemoSchema() {
        assertThat(tablesWithoutPrimaryKeyCheck.check(demoSchema))
            .hasSize(1)
            // HOW TO FIX: add primary key to the table
            .containsExactly(Table.of("demo.payment", 1L));
    }

    @Test
    void getIndexesWithNullValuesShouldReturnNothingForPublicSchema() {
        assertThat(indexesWithNullValuesCheck.check())
            .isEmpty();
    }

    @Test
    void getIndexesWithNullValuesShouldReturnOneRowForDemoSchema() {
        assertThat(indexesWithNullValuesCheck.check(demoSchema))
            .hasSize(1)
            // HOW TO FIX: consider excluding null values from index if it's possible
            .containsExactly(IndexWithNulls.of(BUYER_TABLE, "demo.i_buyer_middle_name", 1L, "middle_name"));
    }

    @Test
    void getTablesWithoutDescriptionShouldReturnTwoRowsForPublicSchema() {
        assertThat(tablesWithoutDescriptionCheck.check())
            .hasSize(1)
            // HOW TO FIX: just add liquibase table to exclusions
            .containsExactlyInAnyOrder(Table.of("databasechangelog", 16_384L));
    }

    @Test
    void getTablesWithoutDescriptionShouldReturnNothingForDemoSchema() {
        assertThat(tablesWithoutDescriptionCheck.check(demoSchema))
            .isEmpty();
    }

    @Test
    void getColumnsWithoutDescriptionShouldReturnSeveralRowsForPublicSchema() {
        assertThat(columnsWithoutDescriptionCheck.check())
            // HOW TO FIX: just add liquibase table to exclusions
            .hasSize(14)
            .allMatch(c -> "databasechangelog".equals(c.getTableName()));
    }

    @Test
    void getColumnsWithoutDescriptionShouldReturnNothingForDemoSchema() {
        assertThat(columnsWithoutDescriptionCheck.check(demoSchema))
            .isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"public", "demo"})
    void getColumnsWithJsonTypeShouldReturnNothingForAllSchemas(@Nonnull final String schemaName) {
        assertThat(columnsWithJsonTypeCheck.check(PgContext.of(schemaName)))
            .isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"public", "demo"})
    void getColumnsWithSerialTypesShouldReturnNothingForAllSchemas(@Nonnull final String schemaName) {
        assertThat(columnsWithSerialTypesCheck.check(PgContext.of(schemaName)))
            .isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"public", "demo"})
    void getFunctionsWithoutDescriptionShouldReturnNothingForAllSchemas(@Nonnull final String schemaName) {
        assertThat(functionsWithoutDescriptionCheck.check(PgContext.of(schemaName)))
            .isEmpty();
    }

    @Test
    void indexesWithBooleanShouldReturnNothingForPublicSchema() {
        assertThat(indexesWithBooleanCheck.check())
            .isEmpty();
    }

    @Test
    void indexesWithBooleanShouldReturnOneRowForDemoSchema() {
        assertThat(indexesWithBooleanCheck.check(demoSchema))
            .hasSize(1)
            .contains(IndexWithColumns.ofSingle(ORDERS_TABLE, "demo.i_orders_preorder", 1L,
                Column.ofNotNull(ORDERS_TABLE, "preorder")));
    }

    @Test
    void notValidConstraintsShouldReturnNothingForPublicSchema() {
        assertThat(notValidConstraintsCheck.check())
            .isEmpty();
    }

    @Test
    void notValidConstraintsShouldReturnOneRowForDemoSchema() {
        assertThat(notValidConstraintsCheck.check(demoSchema))
            .hasSize(1)
            .contains(Constraint.ofType(ORDER_ITEM_TABLE, "order_item_amount_less_than_100", ConstraintType.CHECK));
    }

    @Test
    void getBtreeIndexesOnArrayColumnsShouldReturnNothingForPublicSchema() {
        assertThat(btreeIndexesOnArrayColumnsCheck.check())
            .isEmpty();
    }

    @Test
    void getBtreeIndexesOnArrayColumnsShouldReturnOneRowForDemoSchema() {
        assertThat(btreeIndexesOnArrayColumnsCheck.check(demoSchema))
            .hasSize(1)
            .containsExactly(
                IndexWithColumns.ofSingle(ORDER_ITEM_TABLE, "demo.order_item_categories_idx", 8_192L, Column.ofNullable(ORDER_ITEM_TABLE, "categories")));
    }

    @Test
    void sequenceOverflowShouldReturnNothingForPublicSchema() {
        assertThat(sequenceOverflowCheck.check())
            .isEmpty();
    }

    @Test
    void sequenceOverflowShouldReturnOneRowForDemoSchema() {
        assertThat(sequenceOverflowCheck.check(demoSchema))
            .hasSize(1)
            .containsExactly(SequenceState.of("demo.payment_num_seq", "smallint", 8.44));
    }

    @Test
    void getPrimaryKeysWithSerialTypesShouldReturnNothingForPublicSchema() {
        assertThat(primaryKeysWithSerialTypesCheck.check())
            .isEmpty();
    }

    @Test
    void getPrimaryKeysWithSerialTypesShouldReturnOneRowForDemoSchema() {
        assertThat(primaryKeysWithSerialTypesCheck.check(demoSchema))
            .hasSize(1)
            .containsExactly(ColumnWithSerialType.ofBigSerial(Column.ofNotNull("demo.courier", "id"), "demo.courier_id_seq"));
    }
}
