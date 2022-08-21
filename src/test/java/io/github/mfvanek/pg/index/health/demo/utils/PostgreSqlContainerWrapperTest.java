/*
 * Copyright (c) 2019-2022. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.utils;

import io.github.mfvanek.pg.index.health.demo.support.DatabaseAwareTestBase;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class PostgreSqlContainerWrapperTest extends DatabaseAwareTestBase {

    @Test
    void shouldProvideDataSource() {
        assertThat(getDataSource())
                .isNotNull()
                .isInstanceOf(BasicDataSource.class)
                .satisfies(ds -> {
                    final BasicDataSource basic = (BasicDataSource) ds;
                    assertThat(basic)
                            .isNotNull();
                    assertThat(basic.getUrl())
                            .startsWith("jdbc:postgresql://");
                    assertThat(basic.getUsername())
                            .isNotBlank();
                    assertThat(basic.getPassword())
                            .isNotBlank();
                    assertThat(basic.getDriverClassName())
                            .isEqualTo("org.postgresql.Driver");
                });
    }

    @SuppressWarnings("PMD.CloseResource")
    @Test
    void closeShouldCallContainerMethod() {
        final PostgreSQLContainer<?> mock = Mockito.mock(PostgreSQLContainer.class);
        final PostgreSqlContainerWrapper postgres = new PostgreSqlContainerWrapper(mock);
        assertThatCode(postgres::close)
                .doesNotThrowAnyException();
        Mockito.verify(mock, Mockito.times(1)).close();
    }
}
