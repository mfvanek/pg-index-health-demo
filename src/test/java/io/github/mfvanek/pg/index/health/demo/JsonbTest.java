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

public class JsonbTest extends DatabaseAwareTestBase {

    @Test
    void readingAndWritingJsonb() throws SQLException {
        try (final Connection connection = embeddedPostgres.getTestDatabase().getConnection();
             final Statement statement = connection.createStatement();
             final PreparedStatement updateStatement = connection.prepareStatement("update demo.payment set info = ? where id = ?")) {
            connection.setAutoCommit(false);
            try (final ResultSet resultSet = statement.executeQuery("select * from demo.payment order by id limit 10")) {
                while (resultSet.next()) {
                    final long id = resultSet.getLong("id");
                    final PGobject infoAsObject = (PGobject) resultSet.getObject("info");
                    final String infoAsString = resultSet.getString("info");
                    assertThat(infoAsObject).isNotNull();
                    assertThat(infoAsString).isNotBlank();
                    assertThat(infoAsObject.getValue())
                            .isEqualTo("{\" payment\": {\"date\": \"2022-05-27T18:31:42\", \"result\": \"success\"}}")
                            .isEqualTo(infoAsString);

                    final String withoutWhitespaces = StringUtils.deleteWhitespace(infoAsString);
                    assertThat(withoutWhitespaces).isEqualTo("{\"payment\":{\"date\":\"2022-05-27T18:31:42\",\"result\":\"success\"}}");
                    final PGobject fixedInfoObject = new PGobject();
                    fixedInfoObject.setType("jsonb");
                    fixedInfoObject.setValue(withoutWhitespaces);
                    updateStatement.setObject(1, fixedInfoObject);
                    updateStatement.setLong(2, id);
                    final int result = updateStatement.executeUpdate();
                    assertThat(result).isEqualTo(1);
                }
            }
            connection.commit();
        }
    }
}
