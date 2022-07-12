/*
 * Copyright (c) 2019-2022. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
class JsonbTest extends DatabaseAwareTestBase {

    @Test
    void readingAndWritingJsonb() throws SQLException {
        try (Connection connection = EMBEDDED_POSTGRES.getTestDatabase().getConnection();
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
                    final PGobject fixedInfoObject = new PGobject();
                    fixedInfoObject.setType("jsonb");
                    fixedInfoObject.setValue(withoutWhitespaces);
                    updateStatement.setObject(1, fixedInfoObject);
                    updateStatement.setLong(2, paymentId);
                    final int result = updateStatement.executeUpdate();
                    assertThat(result).isEqualTo(1);
                }
            }
            connection.commit();
        }
    }
}
