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
package org.rulii.sample.fulfillment.service;

import org.springframework.stereotype.Service;

/**
 * Stand-in for a payment processor (slow external call).
 */
@Service
public class PaymentService {

    public PaymentService() {
        super();
    }

    public String charge(String orderId, Double amount) {
        sleep(120);
        if ("ORD-DECLINED".equals(orderId)) {
            throw new PaymentDeclinedException(orderId);
        }
        System.out.printf("  [payment ] charged $%,.2f for %s on %s%n",
                amount, orderId, Thread.currentThread().getName());
        return "PAY-" + Math.abs(orderId.hashCode() % 10_000);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}