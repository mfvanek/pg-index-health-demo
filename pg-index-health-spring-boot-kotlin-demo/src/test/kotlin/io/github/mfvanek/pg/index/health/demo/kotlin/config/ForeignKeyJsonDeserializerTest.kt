/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.config

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.mfvanek.pg.index.health.demo.kotlin.utils.BasePgIndexHealthDemoSpringBootTest
import io.github.mfvanek.pg.model.constraint.ForeignKey
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ForeignKeyJsonDeserializerTest : BasePgIndexHealthDemoSpringBootTest() {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    @Throws(JsonProcessingException::class)
    fun deserializeShouldWork() {
        val original = ForeignKey.ofNotNullColumn("users", "fk_user_role", "role_id")
        val json = objectMapper.writeValueAsString(original)
        val restored = objectMapper.readValue(json, ForeignKey::class.java)
        assertThat(restored)
            .isEqualTo(original)
    }

    @Test
    fun deserializeShouldThrowExceptionWhenNoColumns() {
        val json = """{"tableName":"users","constraintName":"fk_user_role","constraintType":"FOREIGN_KEY","name":"fk_user_role","objectType":"CONSTRAINT","validateSql":"alter table users validate constraint fk_user_role;"}"""

        assertThatThrownBy { objectMapper.readValue(json, ForeignKey::class.java) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("columnsInConstraint cannot be empty")
    }

    @Test
    fun deserializeShouldThrowExceptionWhenColumnsIsNotArray() {
        val json = """{"tableName":"users","constraintName":"fk_user_role","columnsInConstraint":"test","constraintType":"FOREIGN_KEY","name":"fk_user_role","objectType":"CONSTRAINT","validateSql":"alter table users validate constraint fk_user_role;"}"""

        assertThatThrownBy { objectMapper.readValue(json, ForeignKey::class.java) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("columnsInConstraint cannot be empty")
    }
}
