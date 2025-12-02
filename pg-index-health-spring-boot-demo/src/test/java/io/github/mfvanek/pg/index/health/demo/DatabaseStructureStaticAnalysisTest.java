/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo;

import io.github.mfvanek.pg.core.checks.common.DatabaseCheckOnHost;
import io.github.mfvanek.pg.core.checks.common.Diagnostic;
import io.github.mfvanek.pg.index.health.demo.utils.BasePgIndexHealthDemoSpringBootTest;
import io.github.mfvanek.pg.model.column.Column;
import io.github.mfvanek.pg.model.column.ColumnWithSerialType;
import io.github.mfvanek.pg.model.column.ColumnWithType;
import io.github.mfvanek.pg.model.constraint.Constraint;
import io.github.mfvanek.pg.model.constraint.ConstraintType;
import io.github.mfvanek.pg.model.constraint.DuplicatedForeignKeys;
import io.github.mfvanek.pg.model.constraint.ForeignKey;
import io.github.mfvanek.pg.model.context.PgContext;
import io.github.mfvanek.pg.model.dbobject.AnyObject;
import io.github.mfvanek.pg.model.dbobject.DbObject;
import io.github.mfvanek.pg.model.dbobject.PgObjectType;
import io.github.mfvanek.pg.model.index.DuplicatedIndexes;
import io.github.mfvanek.pg.model.index.Index;
import io.github.mfvanek.pg.model.index.IndexWithColumns;
import io.github.mfvanek.pg.model.predicates.SkipLiquibaseTablesPredicate;
import io.github.mfvanek.pg.model.sequence.SequenceState;
import io.github.mfvanek.pg.model.table.Table;
import io.github.mfvanek.pg.model.table.TableWithColumns;
import org.assertj.core.api.ListAssert;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

class DatabaseStructureStaticAnalysisTest extends BasePgIndexHealthDemoSpringBootTest {

    private static final int CUSTOM_CHECKS_COUNT = 2;
    private static final String BUYER_TABLE = "buyer";
    private static final String ORDER_ITEM_TABLE = "order_item";
    private static final String ORDERS_TABLE = "orders";
    private static final String ORDER_ID_COLUMN = "order_id";
    private static final String DICTIONARY_TABLE = "\"dictionary-to-delete\"";
    private static final String COURIER_TABLE = "courier";
    private static final String REPORTS_TABLE = "reports";

    @Autowired
    private PgContext ctx;

    @Autowired
    private List<DatabaseCheckOnHost<? extends @NonNull DbObject>> checks;

    @Test
    @DisplayName("Always check PostgreSQL version in your tests")
    void checkPostgresVersion() {
        final String pgVersion = jdbcTemplate.queryForObject("select version();", String.class);
        assertThat(pgVersion)
            .startsWith("PostgreSQL 18.0");
    }

    @Test
    void databaseStructureCheckForPublicSchema() {
        assertThat(checks)
            .hasSize(Diagnostic.values().length + CUSTOM_CHECKS_COUNT);

        checks.forEach(check ->
            assertThat(check.check(SkipLiquibaseTablesPredicate.ofDefault()))
                .as(check.getName())
                .isEmpty());
    }

    @SuppressWarnings({"checkstyle:CyclomaticComplexity", "checkstyle:LambdaBodyLength", "checkstyle:MethodLength"})
    @Test
    void databaseStructureCheckForDemoSchema() {
        assertThat(checks)
            .hasSize(Diagnostic.values().length + CUSTOM_CHECKS_COUNT);

        checks.stream()
            // Skip all runtime checks except SEQUENCE_OVERFLOW
            .filter(check -> check.getName().equals(Diagnostic.SEQUENCE_OVERFLOW.getName()) || check.isStatic())
            .forEach(check -> {
                final ListAssert<? extends DbObject> checksAssert = assertThat(check.check(ctx))
                    .as(check.getName())
                    .usingRecursiveFieldByFieldElementComparator();

                switch (check.getName()) {
                    case "INVALID_INDEXES" -> checksAssert
                        .asInstanceOf(list(Index.class))
                        .hasSize(1)
                        // HOW TO FIX: drop index concurrently, fix data in the table, then create index concurrently again
                        .containsExactly(
                            Index.of(ctx, BUYER_TABLE, "i_buyer_email")
                        );

                    case "DUPLICATED_INDEXES" -> checksAssert
                        .asInstanceOf(list(DuplicatedIndexes.class))
                        .hasSize(2)
                        // HOW TO FIX: do not manually create index for column with unique constraint
                        .containsExactly(
                            DuplicatedIndexes.of(
                                Index.of(ctx, BUYER_TABLE, "buyer_pkey", 16_384L),
                                Index.of(ctx, BUYER_TABLE, "idx_buyer_pk", 16_384L)),
                            DuplicatedIndexes.of(
                                Index.of(ctx, ORDER_ITEM_TABLE, "i_order_item_sku_order_id_unique", 8_192L),
                                Index.of(ctx, ORDER_ITEM_TABLE, "order_item_sku_order_id_key", 8_192L))
                        );

                    case "INTERSECTED_INDEXES" -> checksAssert
                        .asInstanceOf(list(DuplicatedIndexes.class))
                        .hasSize(3)
                        // HOW TO FIX: consider using an index with a different column order or just delete unnecessary indexes
                        .containsExactly(
                            DuplicatedIndexes.of(
                                Index.of(ctx, BUYER_TABLE, "buyer_pkey", 16_384L),
                                Index.of(ctx, BUYER_TABLE, "i_buyer_id_phone", 16_384L)),
                            DuplicatedIndexes.of(
                                Index.of(ctx, BUYER_TABLE, "i_buyer_first_name", 16_384L),
                                Index.of(ctx, BUYER_TABLE, "i_buyer_names", 16_384L)),
                            DuplicatedIndexes.of(
                                Index.of(ctx, BUYER_TABLE, "i_buyer_id_phone", 16_384L),
                                Index.of(ctx, BUYER_TABLE, "idx_buyer_pk", 16_384L))
                        );

                    case "FOREIGN_KEYS_WITHOUT_INDEX" -> checksAssert
                        .asInstanceOf(list(ForeignKey.class))
                        .hasSize(5)
                        // HOW TO FIX: create indexes on columns under foreign key constraint
                        .containsExactly(
                            ForeignKey.ofNotNullColumn(ctx, ORDER_ITEM_TABLE, "order_item_order_id_fkey", ORDER_ID_COLUMN),
                            ForeignKey.ofNotNullColumn(ctx, ORDER_ITEM_TABLE, "order_item_order_id_fk_duplicate", ORDER_ID_COLUMN),
                            ForeignKey.ofNotNullColumn(ctx, ORDER_ITEM_TABLE, "order_item_warehouse_id_fk", "warehouse_id"),
                            ForeignKey.ofNotNullColumn(ctx, ORDERS_TABLE, "orders_buyer_id_fkey", "buyer_id"),
                            ForeignKey.ofNullableColumn(ctx, "payment", "payment_order_id_fkey", ORDER_ID_COLUMN));

                    case "TABLES_WITHOUT_PRIMARY_KEY" -> checksAssert
                        .asInstanceOf(list(Table.class))
                        .hasSize(2)
                        // HOW TO FIX: add a primary key to the table
                        .containsExactly(
                            Table.of(ctx, DICTIONARY_TABLE),
                            Table.of(ctx, "payment", 4_276_224L)
                        );

                    case "INDEXES_WITH_NULL_VALUES" -> checksAssert
                        .asInstanceOf(list(IndexWithColumns.class))
                        .hasSize(1)
                        // HOW TO FIX: consider excluding null values from the index if it's possible
                        .containsExactly(
                            IndexWithColumns.ofSingle(Index.of(ctx, BUYER_TABLE, "i_buyer_middle_name", 16_384L), Column.ofNullable(ctx, BUYER_TABLE, "middle_name"))
                        );

                    case "INDEXES_WITH_BOOLEAN" -> checksAssert
                        .asInstanceOf(list(IndexWithColumns.class))
                        .hasSize(1)
                        .containsExactly(
                            IndexWithColumns.ofSingle(ctx, ORDERS_TABLE, "i_orders_preorder", 8_192L, Column.ofNotNull(ctx, ORDERS_TABLE, "preorder"))
                        );

                    case "NOT_VALID_CONSTRAINTS" -> checksAssert
                        .asInstanceOf(list(Constraint.class))
                        .hasSize(1)
                        .containsExactly(
                            Constraint.ofType(ctx, ORDER_ITEM_TABLE, "order_item_amount_less_than_100", ConstraintType.CHECK)
                        );

                    case "BTREE_INDEXES_ON_ARRAY_COLUMNS" -> checksAssert
                        .asInstanceOf(list(IndexWithColumns.class))
                        .hasSize(1)
                        .containsExactly(
                            IndexWithColumns.ofSingle(ctx, ORDER_ITEM_TABLE, "order_item_categories_idx", 8_192L, Column.ofNullable(ctx, ORDER_ITEM_TABLE, "categories"))
                        );

                    case "SEQUENCE_OVERFLOW" -> checksAssert
                        .asInstanceOf(list(SequenceState.class))
                        .hasSize(1)
                        .containsExactly(
                            SequenceState.of(ctx, "payment_num_seq", "smallint", 8.44)
                        );

                    case "PRIMARY_KEYS_WITH_SERIAL_TYPES" -> checksAssert
                        .asInstanceOf(list(ColumnWithSerialType.class))
                        .hasSize(1)
                        .containsExactly(
                            ColumnWithSerialType.ofBigSerial(ctx, Column.ofNotNull(ctx, COURIER_TABLE, "id"), "courier_id_seq")
                        );

                    case "DUPLICATED_FOREIGN_KEYS" -> checksAssert
                        .asInstanceOf(list(DuplicatedForeignKeys.class))
                        .hasSize(1)
                        .containsExactly(
                            DuplicatedForeignKeys.of(
                                ForeignKey.ofNotNullColumn(ctx, ORDER_ITEM_TABLE, "order_item_order_id_fk_duplicate", ORDER_ID_COLUMN),
                                ForeignKey.ofNotNullColumn(ctx, ORDER_ITEM_TABLE, "order_item_order_id_fkey", ORDER_ID_COLUMN))
                        );

                    case "POSSIBLE_OBJECT_NAME_OVERFLOW" -> checksAssert
                        .asInstanceOf(list(AnyObject.class))
                        .hasSize(1)
                        .containsExactly(
                            AnyObject.ofType(ctx, "idx_courier_phone_and_email_should_be_unique_very_long_name_tha", PgObjectType.INDEX)
                        );

                    case "FOREIGN_KEYS_WITH_UNMATCHED_COLUMN_TYPE" -> checksAssert
                        .asInstanceOf(list(ForeignKey.class))
                        .hasSize(1)
                        .containsExactly(
                            ForeignKey.ofNotNullColumn(ctx, ORDER_ITEM_TABLE, "order_item_warehouse_id_fk", "warehouse_id")
                        );

                    case "TABLES_NOT_LINKED_TO_OTHERS" -> checksAssert
                        .asInstanceOf(list(Table.class))
                        .hasSize(2)
                        .containsExactly(
                            Table.of(ctx, DICTIONARY_TABLE),
                            Table.of(ctx, REPORTS_TABLE)
                        );

                    case "TABLES_WITH_ZERO_OR_ONE_COLUMN" -> checksAssert
                        .asInstanceOf(list(TableWithColumns.class))
                        .hasSize(1)
                        .containsExactly(
                            TableWithColumns.ofNotNullColumn(ctx, DICTIONARY_TABLE, "\"dict-id\"")
                        );

                    case "OBJECTS_NOT_FOLLOWING_NAMING_CONVENTION" -> checksAssert
                        .asInstanceOf(list(AnyObject.class))
                        .hasSize(3)
                        .containsExactly(
                            AnyObject.ofType(ctx, "\"dictionary-to-delete_dict-id_not_null\"", PgObjectType.CONSTRAINT),
                            AnyObject.ofType(ctx, "\"dictionary-to-delete_dict-id_seq\"", PgObjectType.SEQUENCE),
                            AnyObject.ofType(ctx, DICTIONARY_TABLE, PgObjectType.TABLE)
                        );

                    case "COLUMNS_WITHOUT_DESCRIPTION", "COLUMNS_NOT_FOLLOWING_NAMING_CONVENTION" -> checksAssert
                        .asInstanceOf(list(Column.class))
                        .hasSize(1)
                        .containsExactly(
                            Column.ofNotNull(ctx, DICTIONARY_TABLE, "\"dict-id\"")
                        );

                    case "COLUMNS_WITH_FIXED_LENGTH_VARCHAR" -> checksAssert
                        .asInstanceOf(list(ColumnWithType.class))
                        .hasSize(12)
                        .containsExactly(
                            ColumnWithType.ofVarchar(Column.ofNotNull(ctx, BUYER_TABLE, "email")),
                            ColumnWithType.ofVarchar(Column.ofNotNull(ctx, BUYER_TABLE, "first_name")),
                            ColumnWithType.ofVarchar(Column.ofNullable(ctx, BUYER_TABLE, "ip_address")),
                            ColumnWithType.ofVarchar(Column.ofNotNull(ctx, BUYER_TABLE, "last_name")),
                            ColumnWithType.ofVarchar(Column.ofNullable(ctx, BUYER_TABLE, "middle_name")),
                            ColumnWithType.ofVarchar(Column.ofNotNull(ctx, BUYER_TABLE, "phone")),
                            ColumnWithType.ofVarchar(Column.ofNotNull(ctx, COURIER_TABLE, "email")),
                            ColumnWithType.ofVarchar(Column.ofNotNull(ctx, COURIER_TABLE, "first_name")),
                            ColumnWithType.ofVarchar(Column.ofNotNull(ctx, COURIER_TABLE, "last_name")),
                            ColumnWithType.ofVarchar(Column.ofNotNull(ctx, COURIER_TABLE, "phone")),
                            ColumnWithType.ofVarchar(Column.ofNotNull(ctx, ORDER_ITEM_TABLE, "sku")),
                            ColumnWithType.ofVarchar(Column.ofNotNull(ctx, "warehouse", "name"))
                        );

                    case "ALL_PRIMARY_KEYS_MUST_BE_NAMED_AS_ID" -> checksAssert
                        .asInstanceOf(list(TableWithColumns.class))
                        .hasSize(1)
                        .containsExactly(
                            TableWithColumns.of(
                                Table.of(ctx, REPORTS_TABLE),
                                List.of(
                                    Column.ofNotNull(ctx, REPORTS_TABLE, "report_date"),
                                    Column.ofNotNull(ctx, REPORTS_TABLE, "shop_id")))
                        );

                    default -> checksAssert
                        .isEmpty();
                }
            });
    }
}
