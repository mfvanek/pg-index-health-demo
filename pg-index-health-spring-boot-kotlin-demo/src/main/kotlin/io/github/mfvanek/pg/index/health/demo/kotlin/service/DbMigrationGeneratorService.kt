/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.service

import io.github.mfvanek.pg.generator.DbMigrationGenerator
import io.github.mfvanek.pg.health.checks.common.DatabaseCheckOnCluster
import io.github.mfvanek.pg.index.health.demo.kotlin.dto.ForeignKeyMigrationResponse
import io.github.mfvanek.pg.index.health.demo.kotlin.exception.MigrationException
import io.github.mfvanek.pg.index.health.demo.kotlin.mapper.ForeignKeyMapper
import io.github.mfvanek.pg.model.constraint.ForeignKey
import io.github.mfvanek.pg.model.context.PgContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.SQLException
import javax.sql.DataSource

/**
 * Service for generating database migrations.
 *
 * @property dataSource Data source for database connections
 * @property dbMigrationGenerator Generator for creating migration scripts
 * @property foreignKeysNotCoveredWithIndex Check for foreign keys without indexes
 * @property pgContext PostgreSQL context
 */
@Service
@Transactional(readOnly = true)
class DbMigrationGeneratorService(
    private val dataSource: DataSource,
    private val dbMigrationGenerator: DbMigrationGenerator<ForeignKey>,
    private val foreignKeysNotCoveredWithIndex: DatabaseCheckOnCluster<ForeignKey>,
    private val pgContext: PgContext,
    private val foreignKeyMapper: ForeignKeyMapper
) {

    private val logger = LoggerFactory.getLogger(DbMigrationGeneratorService::class.java)

    /**
     * Generates migrations for foreign keys and validates the result.
     *
     * @return response containing foreign keys before and after migration, and generated migrations
     * @throws MigrationException if there are still foreign keys without indexes after migration
     */
    fun generateMigrationsWithForeignKeysChecked(): ForeignKeyMigrationResponse {
        val keysBefore = getForeignKeysFromDb()
        val migrations = generateMigrations(keysBefore)
        runGeneratedMigrations(migrations)
        val keysAfter = getForeignKeysFromDb()
        if (keysAfter.isNotEmpty()) {
            throw MigrationException("There should be no foreign keys not covered by some index")
        }
        return ForeignKeyMigrationResponse(
            keysBefore.map { foreignKeyMapper.toForeignKeyDto(it) },
            keysAfter.map { foreignKeyMapper.toForeignKeyDto(it) },
            migrations
        )
    }

    /**
     * Gets foreign keys from the database that are not covered with indexes.
     *
     * @return list of foreign keys without indexes
     */
    internal fun getForeignKeysFromDb(): List<ForeignKey> {
        return foreignKeysNotCoveredWithIndex.check(pgContext)
    }

    /**
     * Generates migration scripts for the given foreign keys.
     *
     * @param foreignKeys list of foreign keys to generate migrations for
     * @return list of generated migration scripts
     */
    private fun generateMigrations(foreignKeys: List<ForeignKey>): List<String> {
        val generatedMigrations = dbMigrationGenerator.generate(foreignKeys)
        logger.info("Generated migrations: {}", generatedMigrations)
        return generatedMigrations
    }

    /**
     * Runs the generated migration scripts against the database.
     *
     * @param generatedMigrations list of migration scripts to execute
     */
    private fun runGeneratedMigrations(generatedMigrations: List<String>) {
        try {
            dataSource.connection.use { connection ->
                for (migration in generatedMigrations) {
                    executeMigration(connection, migration)
                }
            }
        } catch (e: SQLException) {
            logger.error("Error getting connection", e)
        }
    }

    /**
     * Executes a single migration script.
     *
     * @param connection database connection
     * @param migration migration script to execute
     */
    private fun executeMigration(connection: java.sql.Connection, migration: String) {
        try {
            connection.createStatement().use { statement ->
                statement.execute(migration)
            }
        } catch (e: SQLException) {
            logger.error("Error running migration", e)
        }
    }
}
