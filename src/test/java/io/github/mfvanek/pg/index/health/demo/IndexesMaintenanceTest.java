/*
 * Copyright (c) 2019-2022. Ivan Vakhrushev and others.
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

import static org.assertj.core.api.Assertions.assertThat;

class IndexesMaintenanceTest extends DatabaseAwareTestBase {

    private static IndexesMaintenanceOnHost indexesMaintenance;
    private static TablesMaintenanceOnHost tablesMaintenance;
    private final PgContext demoSchema = PgContext.of("demo");

    @BeforeAll
    static void setUp() {
        final PgConnection pgConnection = PgConnectionImpl.ofPrimary(EMBEDDED_POSTGRES.getTestDatabase());
        indexesMaintenance = new IndexMaintenanceOnHostImpl(pgConnection);
        tablesMaintenance = new TablesMaintenanceOnHostImpl(pgConnection);
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
        final List<Index> invalidIndexes = indexesMaintenance.getInvalidIndexes();

        assertThat(invalidIndexes)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void getInvalidIndexesShouldReturnOneRowForDemoSchema() {
        final List<Index> invalidIndexes = indexesMaintenance.getInvalidIndexes(demoSchema);

        assertThat(invalidIndexes)
                .isNotNull()
                .hasSize(1);
        // HOW TO FIX: drop index concurrently, fix data in table, then create index concurrently again
        assertThat(invalidIndexes.get(0).getIndexName()).isEqualTo("demo.i_buyer_email");
    }

    @Test
    void getDuplicatedIndexesShouldReturnNothingForPublicSchema() {
        final List<DuplicatedIndexes> duplicatedIndexes = indexesMaintenance.getDuplicatedIndexes();

        assertThat(duplicatedIndexes)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void getDuplicatedIndexesShouldReturnOneRowForDemoSchema() {
        final List<DuplicatedIndexes> duplicatedIndexes = indexesMaintenance.getDuplicatedIndexes(demoSchema);

        assertThat(duplicatedIndexes)
                .isNotNull()
                .hasSize(1);
        // HOW TO FIX: do not manually create index for column with unique constraint
        assertThat(duplicatedIndexes.get(0).getIndexNames())
                .containsExactlyInAnyOrder("demo.i_order_item_sku_order_id_unique", "demo.order_item_sku_order_id_key");
    }

    @Test
    void getIntersectedIndexesShouldReturnNothingForPublicSchema() {
        final List<DuplicatedIndexes> intersectedIndexes = indexesMaintenance.getIntersectedIndexes();

        assertThat(intersectedIndexes)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void getIntersectedIndexesShouldReturnOneRowForDemoSchema() {
        final List<DuplicatedIndexes> intersectedIndexes = indexesMaintenance.getIntersectedIndexes(demoSchema);

        assertThat(intersectedIndexes)
                .isNotNull()
                .hasSize(2);
        // HOW TO FIX: consider using an index with a different column order or just delete unnecessary indexes
        assertThat(intersectedIndexes.get(1).getIndexNames())
                .containsExactlyInAnyOrder("demo.buyer_pkey", "demo.i_buyer_id_phone");
        assertThat(intersectedIndexes.get(0).getIndexNames())
                .containsExactlyInAnyOrder("demo.i_buyer_first_name", "demo.i_buyer_names");
    }

    @Test
    void getForeignKeysNotCoveredWithIndexShouldReturnNothingForPublicSchema() {
        final List<ForeignKey> foreignKeys = indexesMaintenance.getForeignKeysNotCoveredWithIndex();

        assertThat(foreignKeys)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void getForeignKeysNotCoveredWithIndexShouldReturnThreeRowsForDemoSchema() {
        final List<ForeignKey> foreignKeys = indexesMaintenance.getForeignKeysNotCoveredWithIndex(demoSchema);

        assertThat(foreignKeys)
                .isNotNull()
                .hasSize(3);
        // HOW TO FIX: create indexes on columns under foreign key constraint
        assertThat(foreignKeys.stream()
                .map(ForeignKey::getConstraintName)
                .collect(Collectors.toList()))
                .containsExactlyInAnyOrder("order_item_order_id_fkey", "orders_buyer_id_fkey", "payment_order_id_fkey");
    }

    @Test
    void getTablesWithoutPrimaryKeyShouldReturnOneRowForPublicSchema() {
        final List<Table> tables = tablesMaintenance.getTablesWithoutPrimaryKey();

        assertThat(tables)
                .isNotNull()
                .hasSize(1);
        // HOW TO FIX: just add liquibase table to exclusions
        assertThat(tables.get(0).getTableName()).isEqualTo("databasechangelog");
    }

    @Test
    void getTablesWithoutPrimaryKeyShouldReturnOneRowForDemoSchema() {
        final List<Table> tables = tablesMaintenance.getTablesWithoutPrimaryKey(demoSchema);

        assertThat(tables)
                .isNotNull()
                .hasSize(1);
        // HOW TO FIX: add primary key to the table
        assertThat(tables.get(0).getTableName()).isEqualTo("demo.payment");
    }

    @Test
    void getIndexesWithNullValuesShouldReturnNothingForPublicSchema() {
        final List<IndexWithNulls> indexesWithNulls = indexesMaintenance.getIndexesWithNullValues();

        assertThat(indexesWithNulls)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void getIndexesWithNullValuesShouldReturnOneRowForDemoSchema() {
        final List<IndexWithNulls> indexesWithNulls = indexesMaintenance.getIndexesWithNullValues(demoSchema);

        assertThat(indexesWithNulls)
                .isNotNull()
                .hasSize(1);
        // HOW TO FIX: consider excluding null values from index if it's possible
        assertThat(indexesWithNulls.get(0).getIndexName()).isEqualTo("demo.i_buyer_middle_name");
    }
}
