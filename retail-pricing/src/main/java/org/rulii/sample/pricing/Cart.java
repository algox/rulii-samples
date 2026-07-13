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
package org.rulii.sample.pricing;

/**
 * A shopping cart at checkout time. Each JavaBean property of this class
 * becomes a Binding when loaded via bindings.loadProperties(cart).
 */
public class Cart {

    private final double subtotal;
    private final int itemCount;
    private final String memberTier;
    private final String couponCode;

    public Cart(double subtotal, int itemCount, String memberTier, String couponCode) {
        super();
        this.subtotal = subtotal;
        this.itemCount = itemCount;
        this.memberTier = memberTier;
        this.couponCode = couponCode;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public int getItemCount() {
        return itemCount;
    }

    public String getMemberTier() {
        return memberTier;
    }

    public String getCouponCode() {
        return couponCode;
    }
}
