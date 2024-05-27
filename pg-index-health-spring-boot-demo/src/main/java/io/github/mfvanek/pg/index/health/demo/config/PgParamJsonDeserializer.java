/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-spring-boot-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;
import io.github.mfvanek.pg.settings.PgParam;
import io.github.mfvanek.pg.settings.PgParamImpl;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class PgParamJsonDeserializer extends JsonDeserializer<PgParam> {

    @Override
    public PgParam deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext)
        throws IOException {
        final TreeNode treeNode = jsonParser.getCodec().readTree(jsonParser);
        final TextNode name = (TextNode) treeNode.get("name");
        final TextNode value = (TextNode) treeNode.get("value");
        return PgParamImpl.of(name.asText(), value.asText());
    }
}
