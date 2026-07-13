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
package org.rulii.sample.promo;

/**
 * An order at checkout. Promo scripts call these getters directly
 * (e.g. ctx.order.getTotal()).
 */
public class Order {

    private final String orderId;
    private final double total;
    private final boolean firstOrder;
    private final String loyaltyTier;

    public Order(String orderId, double total, boolean firstOrder, String loyaltyTier) {
        super();
        this.orderId = orderId;
        this.total = total;
        this.firstOrder = firstOrder;
        this.loyaltyTier = loyaltyTier;
    }

    public String getOrderId() {
        return orderId;
    }

    public double getTotal() {
        return total;
    }

    public boolean isFirstOrder() {
        return firstOrder;
    }

    public String getLoyaltyTier() {
        return loyaltyTier;
    }
}