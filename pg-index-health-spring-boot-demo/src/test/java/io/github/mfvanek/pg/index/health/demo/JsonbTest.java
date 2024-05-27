/*
 * Copyright (c) 2019-2024. Ivan Vakhrushev and others.
 * https://github.com/mfvanek/pg-index-health-demo
 *
 * Licensed under the Apache License 2.0
 */

package io.github.mfvanek.pg.index.health.demo;

import io.github.mfvanek.pg.index.health.demo.utils.BasePgIndexHealthDemoSpringBootTest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import javax.annotation.Nonnull;

import static org.assertj.core.api.Assertions.assertThat;

class JsonbTest extends BasePgIndexHealthDemoSpringBootTest {

    @Test
    void readingAndWritingJsonb() {
        final List<Payment> payments = jdbcTemplate.query("select * from demo.payment order by id limit 10", (rs, rowNum) ->
            Payment.builder()
                .paymentId(rs.getLong("id"))
                .orderId(rs.getLong("order_id"))
                .status(rs.getInt("status"))
                .createdAt(rs.getObject("created_at", LocalDateTime.class))
                .paymentTotal(rs.getBigDecimal("payment_total"))
                .info(rs.getString("info"))
                .build());
        assertThat(payments)
            .hasSize(10)
            .flatExtracting(Payment::getInfo)
            .allSatisfy(info -> assertThat(info)
                .isEqualTo("{\" payment\": {\"date\": \"2022-05-27T18:31:42\", \"result\": \"success\"}}"));
        payments.forEach(this::checkThatJsonbCanBeSavedToDatabase);
    }

    @SneakyThrows
    private void checkThatJsonbCanBeSavedToDatabase(@Nonnull final Payment payment) {
        final String withoutWhitespaces = StringUtils.deleteWhitespace(payment.getInfo());
        assertThat(withoutWhitespaces)
            .isEqualTo("{\"payment\":{\"date\":\"2022-05-27T18:31:42\",\"result\":\"success\"}}");
        final PGobject fixedInfoObject = new PGobject();
        fixedInfoObject.setType("jsonb");
        fixedInfoObject.setValue(withoutWhitespaces);
        final int count = jdbcTemplate.update("update demo.payment set info = ?::jsonb where id = ?::bigint",
            fixedInfoObject, payment.getPaymentId());
        assertThat(count)
            .isEqualTo(1);
    }

    @Getter
    @RequiredArgsConstructor
    @ToString
    @SuperBuilder
    static class Payment {

        private final long paymentId;
        private final long orderId;
        private final int status;
        private final LocalDateTime createdAt;
        private final BigDecimal paymentTotal;
        private final String info;
    }
}
