/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.checks.custom

import io.github.mfvanek.pg.connection.PgConnection
import io.github.mfvanek.pg.core.checks.common.StandardCheckInfo
import io.github.mfvanek.pg.core.checks.extractors.TableWithColumnsExtractor
import io.github.mfvanek.pg.core.checks.host.AbstractCheckOnHost
import io.github.mfvanek.pg.model.context.PgContext
import io.github.mfvanek.pg.model.table.TableWithColumns
import org.springframework.jdbc.core.simple.JdbcClient
import java.sql.ResultSet

class AllPrimaryKeysMustBeNamedAsIdCheckOnHost(
    pgConnection: PgConnection,
    private val jdbcClient: JdbcClient,
) : AbstractCheckOnHost<TableWithColumns>(
    TableWithColumns::class.java,
    pgConnection,
    StandardCheckInfo.ofStatic(
        "ALL_PRIMARY_KEYS_MUST_BE_NAMED_AS_ID",
        """
                select
                    pc.oid::regclass::text as table_name,
                    pg_table_size(pc.oid) as table_size,
                    array_agg(quote_ident(col.attname) || ',' || col.attnotnull::text order by col.attnum) as columns
                from
                    pg_catalog.pg_constraint c
                    inner join pg_catalog.pg_class pc on pc.oid = c.conrelid
                    inner join pg_catalog.pg_namespace nsp on nsp.oid = pc.relnamespace
                    inner join pg_catalog.pg_attribute col on col.attrelid = pc.oid and col.attnum = any(c.conkey)
                where
                    c.contype = 'p' and /* only primary keys */
                    not pc.relispartition and
                    nsp.nspname = :schema_name_param::text
                group by pc.relname, pc.oid, c.conkey
                having bool_and(col.attname <> 'id') /* the primary key is not named 'id' */
                order by table_name;
        """.trimIndent()
    )
) {
    private val extractor = TableWithColumnsExtractor.of()

    override fun doCheck(pgContext: PgContext): List<TableWithColumns> {
        return jdbcClient.sql(checkInfo.getSqlQuery())
            .param("schema_name_param", pgContext.schemaName)
            .query<TableWithColumns> { rs: ResultSet, rowNum: Int -> extractor.mapRow(rs, rowNum) }
            .list()
    }
}
