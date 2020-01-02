package io.github.mfvanek.pg.index.health.demo;

import io.github.mfvanek.pg.connection.PgConnection;
import io.github.mfvanek.pg.connection.PgConnectionImpl;
import io.github.mfvanek.pg.index.maintenance.IndexMaintenance;
import io.github.mfvanek.pg.index.maintenance.IndexMaintenanceImpl;
import io.github.mfvanek.pg.model.DuplicatedIndexes;
import io.github.mfvanek.pg.model.ForeignKey;
import io.github.mfvanek.pg.model.Index;
import io.github.mfvanek.pg.model.IndexWithNulls;
import io.github.mfvanek.pg.model.PgContext;
import io.github.mfvanek.pg.model.Table;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class IndexesMaintenanceTest extends DatabaseAwareTestBase {

    private static IndexMaintenance indexMaintenance;
    private final PgContext demoSchema = PgContext.of("demo");

    @BeforeAll
    static void setUp() {
        final PgConnection pgConnection = PgConnectionImpl.ofMaster(embeddedPostgres.getTestDatabase());
        indexMaintenance = new IndexMaintenanceImpl(pgConnection);
    }

    @Test
    void getInvalidIndexesShouldReturnNothingForPublicSchema() {
        final List<Index> invalidIndexes = indexMaintenance.getInvalidIndexes();

        assertNotNull(invalidIndexes);
        assertEquals(0, invalidIndexes.size());
    }

    @Test
    void getInvalidIndexesShouldReturnOneRowForDemoSchema() {
        final List<Index> invalidIndexes = indexMaintenance.getInvalidIndexes(demoSchema);

        assertNotNull(invalidIndexes);
        assertEquals(1, invalidIndexes.size());

        // TODO
    }

    @Test
    void getDuplicatedIndexesShouldReturnNothingForPublicSchema() {
        final List<DuplicatedIndexes> duplicatedIndexes = indexMaintenance.getDuplicatedIndexes();

        assertNotNull(duplicatedIndexes);
        assertEquals(0, duplicatedIndexes.size());
    }

    @Test
    void getDuplicatedIndexesShouldReturnOneRowForDemoSchema() {
        final List<DuplicatedIndexes> duplicatedIndexes = indexMaintenance.getDuplicatedIndexes(demoSchema);

        // Assert
        assertNotNull(duplicatedIndexes);
        assertEquals(1, duplicatedIndexes.size());

        // TODO
    }

    @Test
    void getIntersectedIndexesShouldReturnNothingForPublicSchema() {
        final List<DuplicatedIndexes> intersectedIndexes = indexMaintenance.getIntersectedIndexes();

        assertNotNull(intersectedIndexes);
        assertEquals(0, intersectedIndexes.size());
    }

    @Test
    void getIntersectedIndexesShouldReturnOneRowForDemoSchema() {
        final List<DuplicatedIndexes> intersectedIndexes = indexMaintenance.getIntersectedIndexes(demoSchema);

        assertNotNull(intersectedIndexes);
        assertEquals(1, intersectedIndexes.size());

        // TODO
    }

    @Test
    void getForeignKeysNotCoveredWithIndexShouldReturnNothingForPublicSchema() {
        final List<ForeignKey> foreignKeys = indexMaintenance.getForeignKeysNotCoveredWithIndex();

        assertNotNull(foreignKeys);
        assertEquals(0, foreignKeys.size());
    }

    @Test
    void getForeignKeysNotCoveredWithIndexShouldReturnThreeRowsForDemoSchema() {
        final List<ForeignKey> foreignKeys = indexMaintenance.getForeignKeysNotCoveredWithIndex(demoSchema);

        assertNotNull(foreignKeys);
        assertEquals(3, foreignKeys.size());
    }

    @Test
    void getTablesWithoutPrimaryKeyShouldReturnOneRowForPublicSchema() {
        final List<Table> tables = indexMaintenance.getTablesWithoutPrimaryKey();

        assertNotNull(tables);
        assertEquals(1, tables.size());
        assertEquals("databasechangelog", tables.get(0).getTableName());
    }

    @Test
    void getTablesWithoutPrimaryKeyShouldReturnOneRowForDemoSchema() {
        final List<Table> tables = indexMaintenance.getTablesWithoutPrimaryKey(demoSchema);

        assertNotNull(tables);
        assertEquals(1, tables.size());

        // TODO
    }

    @Test
    void getIndexesWithNullValuesShouldReturnNothingForPublicSchema() {
        final List<IndexWithNulls> indexesWithNulls = indexMaintenance.getIndexesWithNullValues();

        assertNotNull(indexesWithNulls);
        assertEquals(0, indexesWithNulls.size());
    }

    @Test
    void getIndexesWithNullValuesShouldReturnOneRowForDemoSchema() {
        final List<IndexWithNulls> indexesWithNulls = indexMaintenance.getIndexesWithNullValues(demoSchema);

        assertNotNull(indexesWithNulls);
        assertEquals(1, indexesWithNulls.size());

        // TODO
    }
}
