/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-spring-boot-demo
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
import io.github.mfvanek.pg.checks.host.TablesWithoutDescriptionCheckOnHost;
import io.github.mfvanek.pg.checks.host.TablesWithoutPrimaryKeyCheckOnHost;
import io.github.mfvanek.pg.index.health.demo.utils.BasePgIndexHealthDemoSpringBootTest;
import io.github.mfvanek.pg.model.PgContext;
import io.github.mfvanek.pg.model.column.Column;
import io.github.mfvanek.pg.model.constraint.Constraint;
import io.github.mfvanek.pg.model.constraint.ConstraintType;
import io.github.mfvanek.pg.model.constraint.ForeignKey;
import io.github.mfvanek.pg.model.index.DuplicatedIndexes;
import io.github.mfvanek.pg.model.index.Index;
import io.github.mfvanek.pg.model.index.IndexWithColumns;
import io.github.mfvanek.pg.model.index.IndexWithNulls;
import io.github.mfvanek.pg.model.index.IndexWithSize;
import io.github.mfvanek.pg.model.table.Table;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "checkstyle:ClassFanOutComplexity", "PMD.ExcessiveImports"})
class IndexesMaintenanceTest extends BasePgIndexHealthDemoSpringBootTest {

    private static final String BUYER_TABLE = "demo.buyer";
    private static final String ORDER_ITEM_TABLE = "demo.order_item";
    private static final String ORDERS_TABLE = "demo.orders";

    private final PgContext demoSchema = PgContext.of("demo");

    @Autowired
    private InvalidIndexesCheckOnHost invalidIndexesCheck;
    @Autowired
    private DuplicatedIndexesCheckOnHost duplicatedIndexesCheck;
    @Autowired
    private IntersectedIndexesCheckOnHost intersectedIndexesCheck;
    @Autowired
    private ForeignKeysNotCoveredWithIndexCheckOnHost foreignKeysNotCoveredWithIndexCheck;
    @Autowired
    private TablesWithoutPrimaryKeyCheckOnHost tablesWithoutPrimaryKeyCheck;
    @Autowired
    private IndexesWithNullValuesCheckOnHost indexesWithNullValuesCheck;
    @Autowired
    private TablesWithoutDescriptionCheckOnHost tablesWithoutDescriptionCheck;
    @Autowired
    private ColumnsWithoutDescriptionCheckOnHost columnsWithoutDescriptionCheck;
    @Autowired
    private ColumnsWithJsonTypeCheckOnHost columnsWithJsonTypeCheck;
    @Autowired
    private ColumnsWithSerialTypesCheckOnHost columnsWithSerialTypesCheck;
    @Autowired
    private FunctionsWithoutDescriptionCheckOnHost functionsWithoutDescriptionCheck;
    @Autowired
    private IndexesWithBooleanCheckOnHost indexesWithBooleanCheck;
    @Autowired
    private NotValidConstraintsCheckOnHost notValidConstraintsCheck;
    @Autowired
    private BtreeIndexesOnArrayColumnsCheckOnHost btreeIndexesOnArrayColumnsCheck;

    @Test
    @DisplayName("Always check PostgreSQL version in your tests")
    void checkPostgresVersion() {
        final String pgVersion = jdbcTemplate.queryForObject("select version();", String.class);
        assertThat(pgVersion)
            .startsWith("PostgreSQL 16.2");
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
    void btreeIndexesOnArrayColumnsShouldReturnNothingForPublicSchema() {
        assertThat(btreeIndexesOnArrayColumnsCheck.check())
            .isEmpty();
    }

    @Test
    void btreeIndexesOnArrayColumnsShouldReturnOneRowForDemoSchema() {
        assertThat(btreeIndexesOnArrayColumnsCheck.check(demoSchema))
            .hasSize(1)
            .containsExactly(
                IndexWithColumns.ofSingle(ORDER_ITEM_TABLE, "demo.order_item_categories_idx", 8_192L, Column.ofNullable(ORDER_ITEM_TABLE, "categories")));
    }
}
