/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mfvanek.pg.index.health.demo.utils.BasePgIndexHealthDemoSpringBootTest;
import io.github.mfvanek.pg.model.constraint.ForeignKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// TODO: add similar test to kotlin demo and other tests too
class ForeignKeyJsonDeserializerTest extends BasePgIndexHealthDemoSpringBootTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deserializeShouldWork() throws JsonProcessingException {
        assertThat(objectMapper.getRegisteredModuleIds())
            .hasSizeGreaterThan(1)
            .contains("PgIndexHealthModelModule");

        final ForeignKey original = ForeignKey.ofNotNullColumn("users", "fk_user_role", "role_id");
        final String json = objectMapper.writeValueAsString(original);
        assertThat(json)
            .isEqualTo("""
                {"constraint":{"tableName":"users","constraintName":"fk_user_role","constraintType":"FOREIGN_KEY"},\
                "columns":[{"tableName":"users","columnName":"role_id","notNull":true}]}""");
        final ForeignKey restored = objectMapper.readValue(json, ForeignKey.class);
        assertThat(restored)
            .isEqualTo(original);
    }

    @Test
    void deserializeShouldThrowExceptionWhenNoColumns() {
        final String json = """
            {"columns":[{"tableName":"users","columnName":"role_id","notNull":true}]}""";

        assertThatThrownBy(() -> objectMapper.readValue(json, ForeignKey.class))
            .isInstanceOf(JsonProcessingException.class)
            .hasMessageStartingWith("Missing required field: constraint");
    }

    @Test
    void deserializeShouldThrowExceptionWhenColumnsIsNotArray() {
        final String json = """
            {"constraint":{"tableName":"users","constraintName":"fk_user_role","constraintType":"FOREIGN_KEY"},\
            "columns":[]}""";

        assertThatThrownBy(() -> objectMapper.readValue(json, ForeignKey.class))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("columns cannot be empty");
    }
}
