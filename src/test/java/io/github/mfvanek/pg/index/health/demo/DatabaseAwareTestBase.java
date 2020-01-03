/*
 * Copyright (c) 2020. Ivan Vakhrushev.
 * https://github.com/mfvanek
 */

package io.github.mfvanek.pg.index.health.demo;

import io.zonky.test.db.postgres.embedded.LiquibasePreparer;
import io.zonky.test.db.postgres.junit5.EmbeddedPostgresExtension;
import io.zonky.test.db.postgres.junit5.PreparedDbExtension;
import org.junit.jupiter.api.extension.RegisterExtension;

abstract class DatabaseAwareTestBase {

    @RegisterExtension
    static final PreparedDbExtension embeddedPostgres =
            EmbeddedPostgresExtension.preparedDatabase(
                    LiquibasePreparer.forClasspathLocation("changelogs/changelog.xml"));
}
