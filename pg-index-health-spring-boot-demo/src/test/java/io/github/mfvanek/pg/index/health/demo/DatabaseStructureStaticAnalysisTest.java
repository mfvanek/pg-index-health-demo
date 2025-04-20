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
import io.github.mfvanek.pg.model.index.IndexWithNulls;
import io.github.mfvanek.pg.model.index.IndexWithSize;
import io.github.mfvanek.pg.model.predicates.SkipLiquibaseTablesPredicate;
import io.github.mfvanek.pg.model.sequence.SequenceState;
import io.github.mfvanek.pg.model.table.Table;
import io.github.mfvanek.pg.model.table.TableWithColumns;
import org.assertj.core.api.ListAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

class DatabaseStructureStaticAnalysisTest extends BasePgIndexHealthDemoSpringBootTest {

    private static final String BUYER_TABLE = "demo.buyer";
    private static final String ORDER_ITEM_TABLE = "demo.order_item";
    private static final String ORDERS_TABLE = "demo.orders";
    private static final String ORDER_ID_COLUMN = "order_id";
    private static final String DICTIONARY_TABLE = "demo.\"dictionary-to-delete\"";
    private static final String COURIER_TABLE = "demo.courier";

    @Autowired
    private PgContext ctx;

    @Autowired
    private List<DatabaseCheckOnHost<? extends DbObject>> checks;

    @Test
    @DisplayName("Always check PostgreSQL version in your tests")
    void checkPostgresVersion() {
        final String pgVersion = jdbcTemplate.queryForObject("select version();", String.class);
        assertThat(pgVersion)
            .startsWith("PostgreSQL 17.2");
    }

    @Test
    void databaseStructureCheckForPublicSchema() {
        assertThat(checks)
            .hasSize(Diagnostic.values().length);

        checks.forEach(check ->
            assertThat(check.check(SkipLiquibaseTablesPredicate.ofPublic()))
                .as(check.getDiagnostic().name())
                .isEmpty());
    }

    @SuppressWarnings({"checkstyle:CyclomaticComplexity", "checkstyle:LambdaBodyLength", "checkstyle:MethodLength"})
    @Test
    void databaseStructureCheckForDemoSchema() {
        assertThat(checks)
            .hasSize(Diagnostic.values().length);

        checks.stream()
            // Skip all runtime checks except SEQUENCE_OVERFLOW
            .filter(check -> check.getDiagnostic() == Diagnostic.SEQUENCE_OVERFLOW || check.isStatic())
            .forEach(check -> {
                final ListAssert<? extends DbObject> checksAssert = assertThat(check.check(ctx))
                    .as(check.getDiagnostic().name());

                switch (check.getDiagnostic()) {
                    case INVALID_INDEXES -> checksAssert
                        .asInstanceOf(list(Index.class))
                        .hasSize(1)
                        // HOW TO FIX: drop index concurrently, fix data in table, then create index concurrently again
                        .containsExactly(Index.of(ctx, BUYER_TABLE, "i_buyer_email"));

                    case DUPLICATED_INDEXES -> checksAssert
                        .asInstanceOf(list(DuplicatedIndexes.class))
                        .hasSize(1)
                        // HOW TO FIX: do not manually create index for column with unique constraint
                        .containsExactly(DuplicatedIndexes.of(
                            IndexWithSize.of(ctx, ORDER_ITEM_TABLE, "i_order_item_sku_order_id_unique"),
                            IndexWithSize.of(ctx, ORDER_ITEM_TABLE, "order_item_sku_order_id_key")));

                    case INTERSECTED_INDEXES -> checksAssert
                        .asInstanceOf(list(DuplicatedIndexes.class))
                        .hasSize(2)
                        // HOW TO FIX: consider using an index with a different column order or just delete unnecessary indexes
                        .containsExactlyInAnyOrder(
                            DuplicatedIndexes.of(
                                IndexWithSize.of(ctx, BUYER_TABLE, "demo.buyer_pkey"),
                                IndexWithSize.of(ctx, BUYER_TABLE, "demo.i_buyer_id_phone")),
                            DuplicatedIndexes.of(
                                IndexWithSize.of(ctx, BUYER_TABLE, "demo.i_buyer_first_name"),
                                IndexWithSize.of(ctx, BUYER_TABLE, "demo.i_buyer_names")));

                    case FOREIGN_KEYS_WITHOUT_INDEX -> checksAssert
                        .asInstanceOf(list(ForeignKey.class))
                        .hasSize(5)
                        // HOW TO FIX: create indexes on columns under foreign key constraint
                        .containsExactlyInAnyOrder(
                            ForeignKey.ofNotNullColumn(ORDER_ITEM_TABLE, "order_item_order_id_fkey", ORDER_ID_COLUMN),
                            ForeignKey.ofNotNullColumn(ORDER_ITEM_TABLE, "order_item_order_id_fk_duplicate", ORDER_ID_COLUMN),
                            ForeignKey.ofNotNullColumn(ORDER_ITEM_TABLE, "order_item_warehouse_id_fk", "warehouse_id"),
                            ForeignKey.ofNotNullColumn(ORDERS_TABLE, "orders_buyer_id_fkey", "buyer_id"),
                            ForeignKey.ofNullableColumn("demo.payment", "payment_order_id_fkey", ORDER_ID_COLUMN));

                    case TABLES_WITHOUT_PRIMARY_KEY -> checksAssert
                        .asInstanceOf(list(Table.class))
                        .hasSize(2)
                        // HOW TO FIX: add primary key to the table
                        .containsExactly(
                            Table.of(ctx, DICTIONARY_TABLE),
                            Table.of(ctx, "payment")
                        );

                    case INDEXES_WITH_NULL_VALUES -> checksAssert
                        .asInstanceOf(list(IndexWithNulls.class))
                        .hasSize(1)
                        // HOW TO FIX: consider excluding null values from index if it's possible
                        .containsExactly(IndexWithNulls.of(ctx, BUYER_TABLE, "demo.i_buyer_middle_name", "middle_name"));

                    case INDEXES_WITH_BOOLEAN -> checksAssert
                        .asInstanceOf(list(IndexWithColumns.class))
                        .hasSize(1)
                        .contains(IndexWithColumns.ofSingle(ORDERS_TABLE, "demo.i_orders_preorder", 1L,
                            Column.ofNotNull(ORDERS_TABLE, "preorder")));

                    case NOT_VALID_CONSTRAINTS -> checksAssert
                        .asInstanceOf(list(Constraint.class))
                        .hasSize(1)
                        .contains(Constraint.ofType(ORDER_ITEM_TABLE, "order_item_amount_less_than_100", ConstraintType.CHECK));

                    case BTREE_INDEXES_ON_ARRAY_COLUMNS -> checksAssert
                        .asInstanceOf(list(IndexWithColumns.class))
                        .hasSize(1)
                        .containsExactly(IndexWithColumns.ofSingle(ORDER_ITEM_TABLE, "demo.order_item_categories_idx", 8_192L,
                            Column.ofNullable(ORDER_ITEM_TABLE, "categories")));

                    case SEQUENCE_OVERFLOW -> checksAssert
                        .asInstanceOf(list(SequenceState.class))
                        .hasSize(1)
                        .containsExactly(SequenceState.of(ctx, "payment_num_seq", "smallint", 8.44));

                    case PRIMARY_KEYS_WITH_SERIAL_TYPES -> checksAssert
                        .asInstanceOf(list(ColumnWithSerialType.class))
                        .hasSize(1)
                        .containsExactly(ColumnWithSerialType.ofBigSerial(Column.ofNotNull(ctx, COURIER_TABLE, "id"), "demo.courier_id_seq"));

                    case DUPLICATED_FOREIGN_KEYS -> checksAssert
                        .asInstanceOf(list(DuplicatedForeignKeys.class))
                        .hasSize(1)
                        .containsExactly(DuplicatedForeignKeys.of(
                            ForeignKey.ofNotNullColumn(ORDER_ITEM_TABLE, "order_item_order_id_fk_duplicate", ORDER_ID_COLUMN),
                            ForeignKey.ofNotNullColumn(ORDER_ITEM_TABLE, "order_item_order_id_fkey", ORDER_ID_COLUMN))
                        );

                    case POSSIBLE_OBJECT_NAME_OVERFLOW -> checksAssert
                        .asInstanceOf(list(AnyObject.class))
                        .hasSize(1)
                        .containsExactly(
                            AnyObject.ofType("demo.idx_courier_phone_and_email_should_be_unique_very_long_name_tha", PgObjectType.INDEX));

                    case FOREIGN_KEYS_WITH_UNMATCHED_COLUMN_TYPE -> checksAssert
                        .asInstanceOf(list(ForeignKey.class))
                        .hasSize(1)
                        .containsExactly(ForeignKey.ofNotNullColumn(ORDER_ITEM_TABLE, "order_item_warehouse_id_fk", "warehouse_id"));

                    case TABLES_NOT_LINKED_TO_OTHERS -> checksAssert
                        .asInstanceOf(list(Table.class))
                        .hasSize(1)
                        .containsExactly(Table.of(ctx, DICTIONARY_TABLE));

                    case TABLES_WITH_ZERO_OR_ONE_COLUMN -> checksAssert
                        .asInstanceOf(list(TableWithColumns.class))
                        .hasSize(1)
                        .containsExactly(TableWithColumns.withoutColumns(ctx, DICTIONARY_TABLE));

                    case OBJECTS_NOT_FOLLOWING_NAMING_CONVENTION -> checksAssert
                        .asInstanceOf(list(AnyObject.class))
                        .hasSize(2)
                        .containsExactly(
                            AnyObject.ofType(ctx, "\"dictionary-to-delete_dict-id_seq\"", PgObjectType.SEQUENCE),
                            AnyObject.ofType(ctx, DICTIONARY_TABLE, PgObjectType.TABLE)
                        );

                    case COLUMNS_WITHOUT_DESCRIPTION, COLUMNS_NOT_FOLLOWING_NAMING_CONVENTION -> checksAssert
                        .asInstanceOf(list(Column.class))
                        .hasSize(1)
                        .containsExactly(
                            Column.ofNotNull(ctx, DICTIONARY_TABLE, "\"dict-id\"")
                        );

                    case COLUMNS_WITH_FIXED_LENGTH_VARCHAR -> checksAssert
                        .asInstanceOf(list(Column.class))
                        .hasSize(12)
                        .containsExactly(
                            Column.ofNotNull(ctx, BUYER_TABLE, "email"),
                            Column.ofNotNull(ctx, BUYER_TABLE, "first_name"),
                            Column.ofNullable(ctx, BUYER_TABLE, "ip_address"),
                            Column.ofNotNull(ctx, BUYER_TABLE, "last_name"),
                            Column.ofNullable(ctx, BUYER_TABLE, "middle_name"),
                            Column.ofNotNull(ctx, BUYER_TABLE, "phone"),
                            Column.ofNotNull(ctx, COURIER_TABLE, "email"),
                            Column.ofNotNull(ctx, COURIER_TABLE, "first_name"),
                            Column.ofNotNull(ctx, COURIER_TABLE, "last_name"),
                            Column.ofNotNull(ctx, COURIER_TABLE, "phone"),
                            Column.ofNotNull(ctx, ORDER_ITEM_TABLE, "sku"),
                            Column.ofNotNull(ctx, "warehouse", "name")
                        );

                    default -> checksAssert.isEmpty();
                }
            });
    }
}
