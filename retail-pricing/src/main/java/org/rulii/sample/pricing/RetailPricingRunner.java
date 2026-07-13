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

import org.rulii.bind.Bindings;
import org.rulii.ruleset.RuleSet;

import java.util.ArrayList;

/**
 * Runs the pricing RuleSet against a handful of carts, exercising every part
 * of the RuleSet lifecycle (see PricingRules for the rule definitions).
 */
public class RetailPricingRunner {

    private static final RuleSet<PricingSummary> PRICING_RULES = PricingRules.build();

    public RetailPricingRunner() {
        super();
    }

    static PricingSummary price(Cart cart) {
        Bindings bindings = Bindings.builder().standard();
        // Each cart property becomes a read-only Binding:
        // subtotal, itemCount, memberTier, couponCode
        bindings.loadProperties(cart);
        // The working state (discountPercent, appliedDiscounts, total) is created
        // by the RuleSet's param defaults - see PricingRules.

        return PRICING_RULES.run(bindings);
    }

    public static void main(String[] args) {
        scenario("Gold member, 12 items, coupon SAVE5 - hits the 20% cap, coupon never runs",
                new Cart(200.00, 12, "GOLD", "SAVE5"));

        scenario("Silver member, 3 items, coupon SAVE5",
                new Cart(100.00, 3, "SILVER", "save5 "));

        scenario("No membership, invalid coupon SAVE99",
                new Cart(80.00, 2, null, "SAVE99"));

        scenario("Gold member, no coupon supplied - couponRule is SKIPPED",
                new Cart(120.00, 2, "GOLD", null));

        scenario("Empty cart - RuleSet pre-condition fails, nothing runs",
                new Cart(0.00, 0, "GOLD", "SAVE5"));

        scenario("Coupon service outage - errorHandler returns a safe fallback",
                new Cart(150.00, 1, null, "CRASH"));
    }

    private static void scenario(String title, Cart cart) {
        System.out.println();
        System.out.println("=== " + title);
        price(cart).print();
    }
}
