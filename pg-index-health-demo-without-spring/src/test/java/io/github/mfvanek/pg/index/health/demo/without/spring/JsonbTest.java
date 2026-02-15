/*
 * Copyright (c) 2019-2026. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.without.spring;

import io.github.mfvanek.pg.index.health.demo.without.spring.support.DatabaseAwareTestBase;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;
import org.postgresql.util.PSQLException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@NullMarked
class JsonbTest extends DatabaseAwareTestBase {

    @Test
    void readingAndWritingJsonb() throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             Statement statement = connection.createStatement();
             PreparedStatement updateStatement = connection.prepareStatement("update demo.payment set info = ? where id = ?")) {
            connection.setAutoCommit(false);
            try (ResultSet resultSet = statement.executeQuery("select * from demo.payment order by id limit 10")) {
                while (resultSet.next()) {
                    final long paymentId = resultSet.getLong("id");
                    final PGobject infoAsObject = (PGobject) resultSet.getObject("info");
                    final String infoAsString = resultSet.getString("info");
                    assertThat(infoAsObject)
                        .isNotNull();
                    assertThat(infoAsString)
                        .isNotBlank();
                    assertThat(infoAsObject.getValue())
                        .isEqualTo("{\" payment\": {\"date\": \"2022-05-27T18:31:42\", \"result\": \"success\"}}")
                        .isEqualTo(infoAsString);

                    final String withoutWhitespaces = StringUtils.deleteWhitespace(infoAsString);
                    assertThat(withoutWhitespaces).isEqualTo("{\"payment\":{\"date\":\"2022-05-27T18:31:42\",\"result\":\"success\"}}");
                    final PGobject fixedInfoObject = preparePgObject(withoutWhitespaces);
                    updateStatement.setObject(1, fixedInfoObject);
                    updateStatement.setLong(2, paymentId);
                    final int result = updateStatement.executeUpdate();
                    assertThat(result).isOne();

                    final OffsetDateTime createdAt = resultSet.getObject("created_at", OffsetDateTime.class);
                    assertThat(createdAt).isNotNull();
                    final ZonedDateTime zonedDateTime = createdAt.atZoneSameInstant(ZoneId.systemDefault());
                    assertThat(zonedDateTime).isNotNull();

                    assertThatThrownBy(() -> resultSet.getObject("created_at", Instant.class))
                        .isInstanceOf(PSQLException.class)
                        .hasMessage("conversion to class java.time.Instant from timestamptz not supported");
                }
            }
            connection.commit();
        }
    }

    private static PGobject preparePgObject(final String withoutWhitespaces) throws SQLException {
        final PGobject fixedInfoObject = new PGobject();
        fixedInfoObject.setType("jsonb");
        fixedInfoObject.setValue(withoutWhitespaces);
        return fixedInfoObject;
    }
}
