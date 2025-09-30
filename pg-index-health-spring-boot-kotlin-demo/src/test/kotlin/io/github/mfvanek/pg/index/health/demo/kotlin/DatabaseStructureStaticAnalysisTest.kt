/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin

import io.github.mfvanek.pg.core.checks.common.DatabaseCheckOnHost
import io.github.mfvanek.pg.core.checks.common.Diagnostic
import io.github.mfvanek.pg.index.health.demo.kotlin.utils.BasePgIndexHealthDemoSpringBootTest
import io.github.mfvanek.pg.model.column.Column
import io.github.mfvanek.pg.model.column.ColumnWithSerialType
import io.github.mfvanek.pg.model.constraint.Constraint
import io.github.mfvanek.pg.model.constraint.ConstraintType
import io.github.mfvanek.pg.model.constraint.DuplicatedForeignKeys
import io.github.mfvanek.pg.model.constraint.ForeignKey
import io.github.mfvanek.pg.model.context.PgContext
import io.github.mfvanek.pg.model.dbobject.AnyObject
import io.github.mfvanek.pg.model.dbobject.DbObject
import io.github.mfvanek.pg.model.dbobject.PgObjectType
import io.github.mfvanek.pg.model.index.DuplicatedIndexes
import io.github.mfvanek.pg.model.index.Index
import io.github.mfvanek.pg.model.index.IndexWithColumns
import io.github.mfvanek.pg.model.predicates.SkipLiquibaseTablesPredicate
import io.github.mfvanek.pg.model.sequence.SequenceState
import io.github.mfvanek.pg.model.table.Table
import io.github.mfvanek.pg.model.table.TableWithColumns
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertTrue

@Suppress("LargeClass")
class DatabaseStructureStaticAnalysisTest : BasePgIndexHealthDemoSpringBootTest() {

    companion object {
        private const val BUYER_TABLE = "demo.buyer"
        private const val ORDER_ITEM_TABLE = "demo.order_item"
        private const val ORDERS_TABLE = "demo.orders"
        private const val ORDER_ID_COLUMN = "order_id"
        private const val DICTIONARY_TABLE = "demo.\"dictionary-to-delete\""
        private const val COURIER_TABLE = "demo.courier"
    }

    @Autowired
    private lateinit var ctx: PgContext

    @Autowired
    private lateinit var checks: List<DatabaseCheckOnHost<out DbObject>>

    @Test
    @DisplayName("Always check PostgreSQL version in your tests")
    fun checkPostgresVersion() {
        val pgVersion = jdbcTemplate.queryForObject("select version();", String::class.java)
        require(pgVersion != null) { "PostgreSQL version should not be null" }
        assertTrue(pgVersion.startsWith("PostgreSQL 17.4"))
    }

    @Test
    fun databaseStructureCheckForPublicSchema() {
        assertTrue(checks.size == Diagnostic.values().size)

        checks.forEach { check ->
            assertTrue(
                check.check(SkipLiquibaseTablesPredicate.ofDefault()).isEmpty(),
                check.diagnostic.name
            )
        }
    }

    @Suppress("CyclomaticComplexity", "LongMethod")
    @Test
    fun databaseStructureCheckForDemoSchema() {
        assertTrue(checks.size == Diagnostic.values().size)

        checks.stream()
            // Skip all runtime checks except SEQUENCE_OVERFLOW
            .filter { check -> check.diagnostic == Diagnostic.SEQUENCE_OVERFLOW || check.isStatic }
            .forEach { check ->
                validateCheckResult(check)
            }
    }

    private val validationMap = mapOf(
        Diagnostic.INVALID_INDEXES to ::validateInvalidIndexes,
        Diagnostic.DUPLICATED_INDEXES to ::validateDuplicatedIndexes,
        Diagnostic.INTERSECTED_INDEXES to ::validateIntersectedIndexes,
        Diagnostic.FOREIGN_KEYS_WITHOUT_INDEX to ::validateForeignKeysWithoutIndex,
        Diagnostic.TABLES_WITHOUT_PRIMARY_KEY to ::validateTablesWithoutPrimaryKey,
        Diagnostic.INDEXES_WITH_NULL_VALUES to ::validateIndexesWithNullValues,
        Diagnostic.INDEXES_WITH_BOOLEAN to ::validateIndexesWithBoolean,
        Diagnostic.NOT_VALID_CONSTRAINTS to ::validateNotValidConstraints,
        Diagnostic.BTREE_INDEXES_ON_ARRAY_COLUMNS to ::validateBtreeIndexesOnArrayColumns,
        Diagnostic.SEQUENCE_OVERFLOW to ::validateSequenceOverflow,
        Diagnostic.PRIMARY_KEYS_WITH_SERIAL_TYPES to ::validatePrimaryKeysWithSerialTypes,
        Diagnostic.DUPLICATED_FOREIGN_KEYS to ::validateDuplicatedForeignKeys,
        Diagnostic.POSSIBLE_OBJECT_NAME_OVERFLOW to ::validatePossibleObjectNameOverflow,
        Diagnostic.FOREIGN_KEYS_WITH_UNMATCHED_COLUMN_TYPE to ::validateForeignKeysWithUnmatchedColumnType,
        Diagnostic.TABLES_NOT_LINKED_TO_OTHERS to ::validateTablesNotLinkedToOthers,
        Diagnostic.TABLES_WITH_ZERO_OR_ONE_COLUMN to ::validateTablesWithZeroOrOneColumn,
        Diagnostic.OBJECTS_NOT_FOLLOWING_NAMING_CONVENTION to ::validateObjectsNotFollowingNamingConvention,
        Diagnostic.COLUMNS_WITHOUT_DESCRIPTION to ::validateColumnsWithoutDescription,
        Diagnostic.COLUMNS_NOT_FOLLOWING_NAMING_CONVENTION to ::validateColumnsWithoutDescription,
        Diagnostic.COLUMNS_WITH_FIXED_LENGTH_VARCHAR to ::validateColumnsWithFixedLengthVarchar
    )

    private fun validateCheckResult(check: DatabaseCheckOnHost<out DbObject>) {
        val validator = validationMap[check.diagnostic]
        if (validator != null) {
            validator.invoke(check)
        } else {
            assertTrue(check.check(ctx).isEmpty())
        }
    }

    private fun validateInvalidIndexes(check: DatabaseCheckOnHost<out DbObject>) {
        assertTrue(check.check(ctx).size == 1)
        val indexes = check.check(ctx) as List<Index>
        assertTrue(
            indexes.contains(
                Index.of(ctx, BUYER_TABLE, "i_buyer_email")
            )
        )
    }

    private fun validateDuplicatedIndexes(check: DatabaseCheckOnHost<out DbObject>) {
        assertTrue(check.check(ctx).size == 2)
        val duplicatedIndexes = check.check(ctx) as List<DuplicatedIndexes>
        assertTrue(
            duplicatedIndexes.contains(
                DuplicatedIndexes.of(
                    Index.of(ctx, BUYER_TABLE, "demo.buyer_pkey"),
                    Index.of(ctx, BUYER_TABLE, "demo.idx_buyer_pk")
                )
            )
        )
        assertTrue(
            duplicatedIndexes.contains(
                DuplicatedIndexes.of(
                    Index.of(ctx, ORDER_ITEM_TABLE, "i_order_item_sku_order_id_unique"),
                    Index.of(ctx, ORDER_ITEM_TABLE, "order_item_sku_order_id_key")
                )
            )
        )
    }

    private fun validateIntersectedIndexes(check: DatabaseCheckOnHost<out DbObject>) {
        assertTrue(check.check(ctx).size == 3)
        val duplicatedIndexes = check.check(ctx) as List<DuplicatedIndexes>
        assertTrue(
            duplicatedIndexes.contains(
                DuplicatedIndexes.of(
                    Index.of(ctx, BUYER_TABLE, "demo.buyer_pkey"),
                    Index.of(ctx, BUYER_TABLE, "demo.i_buyer_id_phone")
                )
            )
        )
        assertTrue(
            duplicatedIndexes.contains(
                DuplicatedIndexes.of(
                    Index.of(ctx, BUYER_TABLE, "demo.i_buyer_first_name"),
                    Index.of(ctx, BUYER_TABLE, "demo.i_buyer_names")
                )
            )
        )
        assertTrue(
            duplicatedIndexes.contains(
                DuplicatedIndexes.of(
                    Index.of(ctx, BUYER_TABLE, "demo.i_buyer_id_phone"),
                    Index.of(ctx, BUYER_TABLE, "demo.idx_buyer_pk")
                )
            )
        )
    }

    private fun validateForeignKeysWithoutIndex(check: DatabaseCheckOnHost<out DbObject>) {
        assertTrue(check.check(ctx).size == 5)
        val foreignKeys = check.check(ctx) as List<ForeignKey>
        assertTrue(
            foreignKeys.contains(
                ForeignKey.ofNotNullColumn(
                    ORDER_ITEM_TABLE,
                    "order_item_order_id_fkey",
                    ORDER_ID_COLUMN
                )
            )
        )
        assertTrue(
            foreignKeys.contains(
                ForeignKey.ofNotNullColumn(
                    ORDER_ITEM_TABLE,
                    "order_item_order_id_fk_duplicate",
                    ORDER_ID_COLUMN
                )
            )
        )
        assertTrue(
            foreignKeys.contains(
                ForeignKey.ofNotNullColumn(
                    ORDER_ITEM_TABLE,
                    "order_item_warehouse_id_fk",
                    "warehouse_id"
                )
            )
        )
        assertTrue(
            foreignKeys.contains(
                ForeignKey.ofNotNullColumn(ORDERS_TABLE, "orders_buyer_id_fkey", "buyer_id")
            )
        )
        assertTrue(
            foreignKeys.contains(
                ForeignKey.ofNullableColumn("demo.payment", "payment_order_id_fkey", ORDER_ID_COLUMN)
            )
        )
    }

    private fun validateTablesWithoutPrimaryKey(check: DatabaseCheckOnHost<out DbObject>) {
        assertTrue(check.check(ctx).size == 2)
        val tables = check.check(ctx) as List<Table>
        assertTrue(tables.contains(Table.of(ctx, DICTIONARY_TABLE)))
        assertTrue(tables.contains(Table.of(ctx, "payment")))
    }

    private fun validateIndexesWithNullValues(check: DatabaseCheckOnHost<out DbObject>) {
        assertTrue(check.check(ctx).size == 1)
        val indexes = check.check(ctx) as List<IndexWithColumns>
        assertTrue(
            indexes.contains(
                IndexWithColumns.ofNullable(ctx, BUYER_TABLE, "demo.i_buyer_middle_name", "middle_name")
            )
        )
    }

    private fun validateIndexesWithBoolean(check: DatabaseCheckOnHost<out DbObject>) {
        assertTrue(check.check(ctx).size == 1)
        val indexes = check.check(ctx) as List<IndexWithColumns>
        assertTrue(
            indexes.contains(
                IndexWithColumns.ofSingle(
                    ORDERS_TABLE,
                    "demo.i_orders_preorder",
                    1L,
                    Column.ofNotNull(ORDERS_TABLE, "preorder")
                )
            )
        )
    }

    private fun validateNotValidConstraints(check: DatabaseCheckOnHost<out DbObject>) {
        assertTrue(check.check(ctx).size == 1)
        val constraints = check.check(ctx) as List<Constraint>
        assertTrue(
            constraints.contains(
                Constraint.ofType(
                    ORDER_ITEM_TABLE,
                    "order_item_amount_less_than_100",
                    ConstraintType.CHECK
                )
            )
        )
    }

    private fun validateBtreeIndexesOnArrayColumns(check: DatabaseCheckOnHost<out DbObject>) {
        assertTrue(check.check(ctx).size == 1)
        val indexes = check.check(ctx) as List<IndexWithColumns>
        assertTrue(
            indexes.contains(
                IndexWithColumns.ofSingle(
                    ORDER_ITEM_TABLE,
                    "demo.order_item_categories_idx",
                    8192L,
                    Column.ofNullable(ORDER_ITEM_TABLE, "categories")
                )
            )
        )
    }

    private fun validateSequenceOverflow(check: DatabaseCheckOnHost<out DbObject>) {
        assertTrue(check.check(ctx).size == 1)
        val sequences = check.check(ctx) as List<SequenceState>
        assertTrue(
            sequences.contains(
                SequenceState.of(ctx, "payment_num_seq", "smallint", 8.44)
            )
        )
    }

    private fun validatePrimaryKeysWithSerialTypes(check: DatabaseCheckOnHost<out DbObject>) {
        assertTrue(check.check(ctx).size == 1)
        val columns = check.check(ctx) as List<ColumnWithSerialType>
        assertTrue(
            columns.contains(
                ColumnWithSerialType.ofBigSerial(
                    Column.ofNotNull(ctx, COURIER_TABLE, "id"),
                    "demo.courier_id_seq"
                )
            )
        )
    }

    private fun validateDuplicatedForeignKeys(check: DatabaseCheckOnHost<out DbObject>) {
        assertTrue(check.check(ctx).size == 1)
        val duplicatedFks = check.check(ctx) as List<DuplicatedForeignKeys>
        assertTrue(
            duplicatedFks.contains(
                DuplicatedForeignKeys.of(
                    ForeignKey.ofNotNullColumn(
                        ORDER_ITEM_TABLE,
                        "order_item_order_id_fk_duplicate",
                        ORDER_ID_COLUMN
                    ),
                    ForeignKey.ofNotNullColumn(
                        ORDER_ITEM_TABLE,
                        "order_item_order_id_fkey",
                        ORDER_ID_COLUMN
                    )
                )
            )
        )
    }

    private fun validatePossibleObjectNameOverflow(check: DatabaseCheckOnHost<out DbObject>) {
        assertTrue(check.check(ctx).size == 1)
        val objects = check.check(ctx) as List<AnyObject>
        assertTrue(
            objects.contains(
                AnyObject.ofType(
                    "demo.idx_courier_phone_and_email_should_be_unique_very_long_name_tha",
                    PgObjectType.INDEX
                )
            )
        )
    }

    private fun validateForeignKeysWithUnmatchedColumnType(check: DatabaseCheckOnHost<out DbObject>) {
        assertTrue(check.check(ctx).size == 1)
        val foreignKeys = check.check(ctx) as List<ForeignKey>
        assertTrue(
            foreignKeys.contains(
                ForeignKey.ofNotNullColumn(
                    ORDER_ITEM_TABLE,
                    "order_item_warehouse_id_fk",
                    "warehouse_id"
                )
            )
        )
    }

    private fun validateTablesNotLinkedToOthers(check: DatabaseCheckOnHost<out DbObject>) {
        assertTrue(check.check(ctx).size == 2)
        val tables = check.check(ctx) as List<Table>
        assertTrue(tables.contains(Table.of(ctx, DICTIONARY_TABLE)))
        assertTrue(tables.contains(Table.of(ctx, "reports")))
    }

    private fun validateTablesWithZeroOrOneColumn(check: DatabaseCheckOnHost<out DbObject>) {
        assertTrue(check.check(ctx).size == 1)
        val tables = check.check(ctx) as List<TableWithColumns>
        assertTrue(
            tables.contains(
                TableWithColumns.withoutColumns(ctx, DICTIONARY_TABLE)
            )
        )
    }

    private fun validateObjectsNotFollowingNamingConvention(check: DatabaseCheckOnHost<out DbObject>) {
        assertTrue(check.check(ctx).size == 2)
        val objects = check.check(ctx) as List<AnyObject>
        assertTrue(
            objects.contains(
                AnyObject.ofType(
                    ctx,
                    "\"dictionary-to-delete_dict-id_seq\"",
                    PgObjectType.SEQUENCE
                )
            )
        )
        assertTrue(
            objects.contains(
                AnyObject.ofType(ctx, DICTIONARY_TABLE, PgObjectType.TABLE)
            )
        )
    }

    private fun validateColumnsWithoutDescription(check: DatabaseCheckOnHost<out DbObject>) {
        assertTrue(check.check(ctx).size == 1)
        val columns = check.check(ctx) as List<Column>
        assertTrue(
            columns.contains(
                Column.ofNotNull(ctx, DICTIONARY_TABLE, "\"dict-id\"")
            )
        )
    }

    private fun validateColumnsWithFixedLengthVarchar(check: DatabaseCheckOnHost<out DbObject>) {
        assertTrue(check.check(ctx).size == 12)
        val columns = check.check(ctx) as List<Column>
        assertTrue(columns.contains(Column.ofNotNull(ctx, BUYER_TABLE, "email")))
        assertTrue(columns.contains(Column.ofNotNull(ctx, BUYER_TABLE, "first_name")))
        assertTrue(columns.contains(Column.ofNullable(ctx, BUYER_TABLE, "ip_address")))
        assertTrue(columns.contains(Column.ofNotNull(ctx, BUYER_TABLE, "last_name")))
        assertTrue(columns.contains(Column.ofNullable(ctx, BUYER_TABLE, "middle_name")))
        assertTrue(columns.contains(Column.ofNotNull(ctx, BUYER_TABLE, "phone")))
        assertTrue(columns.contains(Column.ofNotNull(ctx, COURIER_TABLE, "email")))
        assertTrue(columns.contains(Column.ofNotNull(ctx, COURIER_TABLE, "first_name")))
        assertTrue(columns.contains(Column.ofNotNull(ctx, COURIER_TABLE, "last_name")))
        assertTrue(columns.contains(Column.ofNotNull(ctx, COURIER_TABLE, "phone")))
        assertTrue(columns.contains(Column.ofNotNull(ctx, ORDER_ITEM_TABLE, "sku")))
        assertTrue(columns.contains(Column.ofNotNull(ctx, "warehouse", "name")))
    }
}
