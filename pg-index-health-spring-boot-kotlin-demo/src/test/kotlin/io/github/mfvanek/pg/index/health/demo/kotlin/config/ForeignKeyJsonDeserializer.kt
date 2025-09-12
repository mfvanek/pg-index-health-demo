/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.kotlin.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.TextNode
import io.github.mfvanek.pg.model.column.Column
import io.github.mfvanek.pg.model.constraint.ForeignKey
import org.springframework.boot.jackson.JsonComponent
import java.io.IOException
import java.util.ArrayList

@JsonComponent
class ForeignKeyJsonDeserializer : JsonDeserializer<ForeignKey>() {

    @Throws(IOException::class)
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): ForeignKey {
        val codec: ObjectCodec = jsonParser.codec
        val treeNode: JsonNode = codec.readTree(jsonParser)
        val tableName = treeNode["tableName"] as TextNode
        val constraintName = treeNode["constraintName"] as TextNode
        val columns: MutableList<Column> = ArrayList()
        val columnsNode = treeNode["columns"]
        if (columnsNode != null && columnsNode.isArray) {
            for (columnNode in columnsNode) {
                val column: Column = codec.treeToValue(columnNode, Column::class.java)
                columns.add(column)
            }
        }
        return ForeignKey.of(tableName.asText(), constraintName.asText(), columns)
    }
}
