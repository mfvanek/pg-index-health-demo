/*
 * Copyright (c) 2019-2021. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo;

import io.github.mfvanek.pg.connection.PgConnection;
import io.github.mfvanek.pg.connection.PgConnectionImpl;
import io.github.mfvanek.pg.index.maintenance.IndexMaintenanceOnHostImpl;
import io.github.mfvanek.pg.index.maintenance.IndexesMaintenanceOnHost;
import io.github.mfvanek.pg.model.PgContext;
import io.github.mfvanek.pg.model.index.DuplicatedIndexes;
import io.github.mfvanek.pg.model.index.ForeignKey;
import io.github.mfvanek.pg.model.index.Index;
import io.github.mfvanek.pg.model.index.IndexWithNulls;
import io.github.mfvanek.pg.model.table.Table;
import io.github.mfvanek.pg.table.maintenance.TablesMaintenanceOnHost;
import io.github.mfvanek.pg.table.maintenance.TablesMaintenanceOnHostImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class IndexesMaintenanceTest extends DatabaseAwareTestBase {

    private static IndexesMaintenanceOnHost indexesMaintenance;
    private static TablesMaintenanceOnHost tablesMaintenance;
    private final PgContext demoSchema = PgContext.of("demo");

    @BeforeAll
    static void setUp() {
        final PgConnection pgConnection = PgConnectionImpl.ofPrimary(embeddedPostgres.getTestDatabase());
        indexesMaintenance = new IndexMaintenanceOnHostImpl(pgConnection);
        tablesMaintenance = new TablesMaintenanceOnHostImpl(pgConnection);
    }

    @Test
    @DisplayName("Always check PostgreSQL version in your tests")
    void checkPostgresVersion() throws SQLException {
        try (final Connection connection = embeddedPostgres.getTestDatabase().getConnection();
             final Statement statement = connection.createStatement()) {
            try (final ResultSet resultSet = statement.executeQuery("select version();")) {
                resultSet.next();
                final String pgVersion = resultSet.getString(1);
                assertThat(pgVersion, startsWith("PostgreSQL 13.2"));
            }
        }
    }

    @Test
    void getInvalidIndexesShouldReturnNothingForPublicSchema() {
        final List<Index> invalidIndexes = indexesMaintenance.getInvalidIndexes();

        assertNotNull(invalidIndexes);
        assertEquals(0, invalidIndexes.size());
    }

    @Test
    void getInvalidIndexesShouldReturnOneRowForDemoSchema() {
        final List<Index> invalidIndexes = indexesMaintenance.getInvalidIndexes(demoSchema);

        assertNotNull(invalidIndexes);
        assertEquals(1, invalidIndexes.size());
        // HOW TO FIX: drop index concurrently, fix data in table, create index concurrently again
        assertEquals("demo.i_buyer_email", invalidIndexes.get(0).getIndexName());
    }

    @Test
    void getDuplicatedIndexesShouldReturnNothingForPublicSchema() {
        final List<DuplicatedIndexes> duplicatedIndexes = indexesMaintenance.getDuplicatedIndexes();

        assertNotNull(duplicatedIndexes);
        assertEquals(0, duplicatedIndexes.size());
    }

    @Test
    void getDuplicatedIndexesShouldReturnOneRowForDemoSchema() {
        final List<DuplicatedIndexes> duplicatedIndexes = indexesMaintenance.getDuplicatedIndexes(demoSchema);

        assertNotNull(duplicatedIndexes);
        assertEquals(1, duplicatedIndexes.size());
        // HOW TO FIX: do not manually create index for column with unique constraint
        assertThat(duplicatedIndexes.get(0).getIndexNames(), containsInAnyOrder(
                "demo.i_order_item_sku_order_id_unique", "demo.order_item_sku_order_id_key"));
    }

    @Test
    void getIntersectedIndexesShouldReturnNothingForPublicSchema() {
        final List<DuplicatedIndexes> intersectedIndexes = indexesMaintenance.getIntersectedIndexes();

        assertNotNull(intersectedIndexes);
        assertEquals(0, intersectedIndexes.size());
    }

    @Test
    void getIntersectedIndexesShouldReturnOneRowForDemoSchema() {
        final List<DuplicatedIndexes> intersectedIndexes = indexesMaintenance.getIntersectedIndexes(demoSchema);

        assertNotNull(intersectedIndexes);
        assertEquals(2, intersectedIndexes.size());
        // HOW TO FIX: consider using an index with a different column order or just delete unnecessary indexes
        assertThat(intersectedIndexes.get(1).getIndexNames(), contains(
                "demo.buyer_pkey", "demo.i_buyer_id_phone"));
        assertThat(intersectedIndexes.get(0).getIndexNames(), contains(
                "demo.i_buyer_first_name", "demo.i_buyer_names"));
    }

    @Test
    void getForeignKeysNotCoveredWithIndexShouldReturnNothingForPublicSchema() {
        final List<ForeignKey> foreignKeys = indexesMaintenance.getForeignKeysNotCoveredWithIndex();

        assertNotNull(foreignKeys);
        assertEquals(0, foreignKeys.size());
    }

    @Test
    void getForeignKeysNotCoveredWithIndexShouldReturnThreeRowsForDemoSchema() {
        final List<ForeignKey> foreignKeys = indexesMaintenance.getForeignKeysNotCoveredWithIndex(demoSchema);

        assertNotNull(foreignKeys);
        assertEquals(3, foreignKeys.size());
        // HOW TO FIX: create indexes on columns under foreign key constraint
        assertThat(foreignKeys.stream()
                .map(ForeignKey::getConstraintName)
                .collect(Collectors.toList()), containsInAnyOrder(
                "order_item_order_id_fkey", "orders_buyer_id_fkey", "payment_order_id_fkey"));
    }

    @Test
    void getTablesWithoutPrimaryKeyShouldReturnOneRowForPublicSchema() {
        final List<Table> tables = tablesMaintenance.getTablesWithoutPrimaryKey();

        assertNotNull(tables);
        assertEquals(1, tables.size());
        // HOW TO FIX: just add liquibase table to exclusions
        assertEquals("databasechangelog", tables.get(0).getTableName());
    }

    @Test
    void getTablesWithoutPrimaryKeyShouldReturnOneRowForDemoSchema() {
        final List<Table> tables = tablesMaintenance.getTablesWithoutPrimaryKey(demoSchema);

        assertNotNull(tables);
        assertEquals(1, tables.size());
        // HOW TO FIX: add primary key to the table
        assertEquals("demo.payment", tables.get(0).getTableName());
    }

    @Test
    void getIndexesWithNullValuesShouldReturnNothingForPublicSchema() {
        final List<IndexWithNulls> indexesWithNulls = indexesMaintenance.getIndexesWithNullValues();

        assertNotNull(indexesWithNulls);
        assertEquals(0, indexesWithNulls.size());
    }

    @Test
    void getIndexesWithNullValuesShouldReturnOneRowForDemoSchema() {
        final List<IndexWithNulls> indexesWithNulls = indexesMaintenance.getIndexesWithNullValues(demoSchema);

        assertNotNull(indexesWithNulls);
        assertEquals(1, indexesWithNulls.size());
        // HOW TO FIX: consider excluding null values from index if it's possible
        assertEquals("demo.i_buyer_middle_name", indexesWithNulls.get(0).getIndexName());
    }
}
