/*
 * Copyright (c) 2019-2022. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo;

import io.zonky.test.db.postgres.embedded.LiquibasePreparer;
import io.zonky.test.db.postgres.junit5.EmbeddedPostgresExtension;
import io.zonky.test.db.postgres.junit5.PreparedDbExtension;
import org.junit.jupiter.api.extension.RegisterExtension;

public abstract class DatabaseAwareTestBase {

    @RegisterExtension
    protected static final PreparedDbExtension EMBEDDED_POSTGRES =
            EmbeddedPostgresExtension.preparedDatabase(
                    LiquibasePreparer.forClasspathLocation("changelogs/changelog.xml"));
}
