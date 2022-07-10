/*
 * Copyright (c) 2019-2022. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo;

import io.github.mfvanek.pg.checks.host.ColumnsWithoutDescriptionCheckOnHost;
import io.github.mfvanek.pg.checks.host.DuplicatedIndexesCheckOnHost;
import io.github.mfvanek.pg.checks.host.ForeignKeysNotCoveredWithIndexCheckOnHost;
import io.github.mfvanek.pg.checks.host.IndexesWithNullValuesCheckOnHost;
import io.github.mfvanek.pg.checks.host.IntersectedIndexesCheckOnHost;
import io.github.mfvanek.pg.checks.host.InvalidIndexesCheckOnHost;
import io.github.mfvanek.pg.checks.host.TablesWithoutDescriptionCheckOnHost;
import io.github.mfvanek.pg.checks.host.TablesWithoutPrimaryKeyCheckOnHost;
import io.github.mfvanek.pg.connection.PgConnection;
import io.github.mfvanek.pg.connection.PgConnectionImpl;
import io.github.mfvanek.pg.model.PgContext;
import io.github.mfvanek.pg.model.index.DuplicatedIndexes;
import io.github.mfvanek.pg.model.index.ForeignKey;
import io.github.mfvanek.pg.model.index.Index;
import io.github.mfvanek.pg.model.index.IndexWithNulls;
import io.github.mfvanek.pg.model.index.IndexWithSize;
import io.github.mfvanek.pg.model.table.Column;
import io.github.mfvanek.pg.model.table.Table;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IndexesMaintenanceTest extends DatabaseAwareTestBase {

    private static final String BUYER_TABLE = "demo.buyer";
    private static final String ORDER_ITEM_TABLE = "demo.order_item";

    private final PgContext demoSchema = PgContext.of("demo");
    private final PgConnection pgConnection;

    IndexesMaintenanceTest() {
        this.pgConnection = PgConnectionImpl.ofPrimary(EMBEDDED_POSTGRES.getTestDatabase());
    }

    @Test
    @DisplayName("Always check PostgreSQL version in your tests")
    void checkPostgresVersion() throws SQLException {
        try (Connection connection = EMBEDDED_POSTGRES.getTestDatabase().getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("select version();")) {
                resultSet.next();
                final String pgVersion = resultSet.getString(1);
                assertThat(pgVersion).startsWith("PostgreSQL 13.2");
            }
        }
    }

    @Test
    void getInvalidIndexesShouldReturnNothingForPublicSchema() {
        final List<Index> invalidIndexes = new InvalidIndexesCheckOnHost(pgConnection).check();

        assertThat(invalidIndexes)
                .isEmpty();
    }

    @Test
    void getInvalidIndexesShouldReturnOneRowForDemoSchema() {
        final List<Index> invalidIndexes = new InvalidIndexesCheckOnHost(pgConnection).check(demoSchema);

        assertThat(invalidIndexes)
                .hasSize(1)
                // HOW TO FIX: drop index concurrently, fix data in table, then create index concurrently again
                .containsExactly(Index.of(BUYER_TABLE, "demo.i_buyer_email"));
    }

    @Test
    void getDuplicatedIndexesShouldReturnNothingForPublicSchema() {
        final List<DuplicatedIndexes> duplicatedIndexes = new DuplicatedIndexesCheckOnHost(pgConnection).check();

        assertThat(duplicatedIndexes)
                .isEmpty();
    }

    @Test
    void getDuplicatedIndexesShouldReturnOneRowForDemoSchema() {
        final List<DuplicatedIndexes> duplicatedIndexes = new DuplicatedIndexesCheckOnHost(pgConnection).check(demoSchema);

        assertThat(duplicatedIndexes)
                .hasSize(1)
                // HOW TO FIX: do not manually create index for column with unique constraint
                .containsExactly(DuplicatedIndexes.of(
                        IndexWithSize.of(ORDER_ITEM_TABLE, "demo.i_order_item_sku_order_id_unique", 8_192L),
                        IndexWithSize.of(ORDER_ITEM_TABLE, "demo.order_item_sku_order_id_key", 8_192L)));
    }

    @Test
    void getIntersectedIndexesShouldReturnNothingForPublicSchema() {
        final List<DuplicatedIndexes> intersectedIndexes = new IntersectedIndexesCheckOnHost(pgConnection).check();

        assertThat(intersectedIndexes)
                .isEmpty();
    }

    @Test
    void getIntersectedIndexesShouldReturnOneRowForDemoSchema() {
        final List<DuplicatedIndexes> intersectedIndexes = new IntersectedIndexesCheckOnHost(pgConnection).check(demoSchema);

        assertThat(intersectedIndexes)
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
        final List<ForeignKey> foreignKeys = new ForeignKeysNotCoveredWithIndexCheckOnHost(pgConnection).check();

        assertThat(foreignKeys)
                .isEmpty();
    }

    @Test
    void getForeignKeysNotCoveredWithIndexShouldReturnThreeRowsForDemoSchema() {
        final List<ForeignKey> foreignKeys = new ForeignKeysNotCoveredWithIndexCheckOnHost(pgConnection).check(demoSchema);

        assertThat(foreignKeys)
                .hasSize(3)
                // HOW TO FIX: create indexes on columns under foreign key constraint
                .containsExactlyInAnyOrder(
                        ForeignKey.ofNotNullColumn(ORDER_ITEM_TABLE, "order_item_order_id_fkey", "order_id"),
                        ForeignKey.ofNotNullColumn("demo.orders", "orders_buyer_id_fkey", "buyer_id"),
                        ForeignKey.ofNullableColumn("demo.payment", "payment_order_id_fkey", "order_id"));
    }

    @Test
    void getTablesWithoutPrimaryKeyShouldReturnOneRowForPublicSchema() {
        final List<Table> tables = new TablesWithoutPrimaryKeyCheckOnHost(pgConnection).check();

        assertThat(tables)
                .hasSize(1)
                // HOW TO FIX: just add liquibase table to exclusions
                .containsExactly(Table.of("databasechangelog", 1L));
    }

    @Test
    void getTablesWithoutPrimaryKeyShouldReturnOneRowForDemoSchema() {
        final List<Table> tables = new TablesWithoutPrimaryKeyCheckOnHost(pgConnection).check(demoSchema);

        assertThat(tables)
                .hasSize(1)
                // HOW TO FIX: add primary key to the table
                .containsExactly(Table.of("demo.payment", 1L));
    }

    @Test
    void getIndexesWithNullValuesShouldReturnNothingForPublicSchema() {
        final List<IndexWithNulls> indexesWithNulls = new IndexesWithNullValuesCheckOnHost(pgConnection).check();

        assertThat(indexesWithNulls)
                .isEmpty();
    }

    @Test
    void getIndexesWithNullValuesShouldReturnOneRowForDemoSchema() {
        final List<IndexWithNulls> indexesWithNulls = new IndexesWithNullValuesCheckOnHost(pgConnection).check(demoSchema);

        assertThat(indexesWithNulls)
                .hasSize(1)
                // HOW TO FIX: consider excluding null values from index if it's possible
                .containsExactly(IndexWithNulls.of(BUYER_TABLE, "demo.i_buyer_middle_name", 1L, "middle_name"));
    }

    @Test
    void getTablesWithoutDescriptionShouldReturnTwoRowsForPublicSchema() {
        final List<Table> tables = new TablesWithoutDescriptionCheckOnHost(pgConnection).check();

        assertThat(tables)
                .hasSize(2)
                // HOW TO FIX: just add liquibase table to exclusions
                .containsExactlyInAnyOrder(
                        Table.of("databasechangelog", 16_384L),
                        Table.of("databasechangeloglock", 8_192L));
    }

    @Test
    void getTablesWithoutDescriptionShouldReturnOneRowForDemoSchema() {
        final List<Table> tables = new TablesWithoutDescriptionCheckOnHost(pgConnection).check(demoSchema);

        assertThat(tables)
                .isEmpty();
    }

    @Test
    void getColumnsWithoutDescriptionShouldReturnSeveralRowsForPublicSchema() {
        final List<Column> columns = new ColumnsWithoutDescriptionCheckOnHost(pgConnection).check();

        assertThat(columns)
                // HOW TO FIX: just add liquibase table to exclusions
                .hasSize(18)
                .allMatch(c -> "databasechangelog".equals(c.getTableName()) || "databasechangeloglock".equals(c.getTableName()));
    }

    @Test
    void getColumnsWithoutDescriptionShouldReturnSeveralRowsForDemoSchema() {
        final List<Column> columns = new ColumnsWithoutDescriptionCheckOnHost(pgConnection).check(demoSchema);

        assertThat(columns)
                .isEmpty();
    }
}
