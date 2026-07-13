/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2026, Algorithmx Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rulii.sample.fulfillment;

import org.junit.jupiter.api.Test;
import org.rulii.sample.fulfillment.model.FulfillmentResult;
import org.rulii.sample.fulfillment.service.FulfillmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Rules become testable like any other Spring code. The fixed Clock bean
 * (honored by both rulii's context options and the @Autowired rule field)
 * pins "today", so the delivery estimate is deterministic.
 */
@SpringBootTest
class FulfillmentFlowTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 1);

    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(Instant.parse("2026-07-01T10:00:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired
    private FulfillmentService fulfillmentService;

    @Test
    void inStockOrderShipsWithDeterministicDeliveryDate() {
        FulfillmentResult result = fulfillmentService.fulfill("ORD-1001", 650.00);

        assertEquals("SHIPPED", result.status());
        assertFalse(result.paymentRef().isEmpty());
        assertFalse(result.trackingNumber().isEmpty());
        // fixed clock (2026-07-01) + fulfillment.delivery-days (3)
        assertEquals(TODAY.plusDays(3), result.estimatedDelivery());
        assertTrue(result.notes().contains("High value order - shipping insurance added"));
    }

    @Test
    void outOfStockOrderIsBackordered() {
        FulfillmentResult result = fulfillmentService.fulfill("ORD-OOS", 80.00);

        assertEquals("BACKORDERED", result.status());
        assertTrue(result.paymentRef().isEmpty(), "nothing should be charged");
    }

    @Test
    void declinedPaymentFailsCleanly() {
        FulfillmentResult result = fulfillmentService.fulfill("ORD-DECLINED", 120.00);

        assertEquals("PAYMENT_FAILED", result.status());
        assertTrue(result.notes().contains("Payment declined by processor"));
    }
}