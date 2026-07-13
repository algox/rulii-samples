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

import java.util.List;

/**
 * The outcome of running the pricing RuleSet — built by the resultExtractor.
 */
public record PricingSummary(double subtotal,
                             double discountPercent,
                             List<String> appliedDiscounts,
                             double total,
                             List<String> ruleTrace) {

    public void print() {
        System.out.printf("  Subtotal        : $%,.2f%n", subtotal);
        if (appliedDiscounts.isEmpty()) {
            System.out.println("  Discounts       : (none)");
        } else {
            appliedDiscounts.forEach(d -> System.out.println("  Discount        : " + d));
        }
        System.out.printf("  Total discount  : %.0f%%%n", discountPercent);
        System.out.printf("  Total           : $%,.2f%n", total);
        System.out.println("  Rules evaluated : " + (ruleTrace.isEmpty() ? "(none)" : String.join(", ", ruleTrace)));
    }
}
