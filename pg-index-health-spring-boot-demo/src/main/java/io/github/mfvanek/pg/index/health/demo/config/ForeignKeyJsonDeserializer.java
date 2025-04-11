/*
 * Copyright (c) 2019-2025. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.github.mfvanek.pg.model.column.Column;
import io.github.mfvanek.pg.model.constraint.ForeignKey;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@JsonComponent
public class ForeignKeyJsonDeserializer extends JsonDeserializer<ForeignKey> {

    @Override
    public ForeignKey deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext)
        throws IOException {
        final ObjectCodec codec = jsonParser.getCodec();
        final JsonNode treeNode = codec.readTree(jsonParser);
        final TextNode tableName = (TextNode) treeNode.get("tableName");
        final TextNode constraintName = (TextNode) treeNode.get("constraintName");
        final List<Column> columns = new ArrayList<>();
        final JsonNode columnsNode = treeNode.get("columnsInConstraint");
        if (columnsNode != null && columnsNode.isArray()) {
            for (final JsonNode columnNode : columnsNode) {
                final Column column = codec.treeToValue(columnNode, Column.class);
                columns.add(column);
            }
        }
        return ForeignKey.of(tableName.asText(), constraintName.asText(), columns);
    }
}
