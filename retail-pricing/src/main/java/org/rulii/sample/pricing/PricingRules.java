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
import org.rulii.rule.Rule;
import org.rulii.ruleset.RuleSet;
import org.rulii.ruleset.RuleSetExecutionStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.rulii.model.action.Actions.action;
import static org.rulii.model.condition.Conditions.condition;
import static org.rulii.model.function.Functions.function;

/**
 * Builds the checkout pricing RuleSet.
 *
 * The RuleSet lifecycle, in execution order:
 *
 *   params         declare (and type-check) the inputs the RuleSet expects
 *   preCondition   should this RuleSet run at all? (false -> nothing runs)
 *   initializer    one-time setup before any rule runs
 *   rules          run in declaration order, talking to each other via Bindings
 *   stopCondition  checked after every rule; true -> stop early
 *   finalizer      always runs after the rules (even if a rule blew up)
 *   resultExtractor turns the final Bindings into the caller's return value
 *   errorHandler   converts an execution error into a fallback result
 */
public final class PricingRules {

    /** Discounts never exceed this; also the early-stop threshold. */
    static final double MAX_DISCOUNT_PERCENT = 20.0;

    private static final Map<String, Double> COUPONS = Map.of("SAVE5", 5.0, "SAVE15", 15.0);

    private PricingRules() {
        super();
    }

    public static RuleSet<PricingSummary> build() {
        return RuleSet.builder()
                .with("pricingRules", "Computes checkout discounts for a shopping cart.")

                // Declared inputs. Wrong/missing types fail fast before any rule runs.
                .param("subtotal", Double.class, true)
                .param("itemCount", Integer.class, true)
                .param("memberTier", String.class)
                .param("couponCode", String.class)
                // Working state, created automatically when the caller doesn't
                // supply it. Defaults may be computed from other bindings.
                .param("discountPercent", Double.class, function(() -> 0.0))
                .param("appliedDiscounts", List.class, function(() -> new ArrayList<String>()))
                .param("total", Double.class, function((Double subtotal) -> subtotal))

                // An empty cart has nothing to price - skip the whole RuleSet.
                .preCondition(condition((Double subtotal) -> subtotal > 0))

                // Runs once, before the first rule: normalize the coupon code into a
                // new "coupon" binding. It lives in the RuleSet's scope and is gone
                // once the run completes.
                .initializer(action((Bindings bindings, String couponCode) ->
                        bindings.bind("coupon", String.class,
                                couponCode == null ? null : couponCode.trim().toUpperCase())))

                .rule(Rule.builder()
                        .name("goldMemberRule", "Gold members get 10% off.")
                        .given(condition((String memberTier) -> "GOLD".equals(memberTier)))
                        .then(action((Bindings bindings, Double discountPercent, List<String> appliedDiscounts) -> {
                            bindings.setValue("discountPercent", discountPercent + 10.0);
                            appliedDiscounts.add("Gold member (-10%)");
                        }))
                        .build())

                .rule(Rule.builder()
                        .name("silverMemberRule", "Silver members get 5% off.")
                        .given(condition((String memberTier) -> "SILVER".equals(memberTier)))
                        .then(action((Bindings bindings, Double discountPercent, List<String> appliedDiscounts) -> {
                            bindings.setValue("discountPercent", discountPercent + 5.0);
                            appliedDiscounts.add("Silver member (-5%)");
                        }))
                        .build())

                .rule(Rule.builder()
                        .name("bulkOrderRule", "10 items or more gets 10% off.")
                        .given(condition((Integer itemCount) -> itemCount >= 10))
                        .then(action((Bindings bindings, Double discountPercent, List<String> appliedDiscounts) -> {
                            bindings.setValue("discountPercent", discountPercent + 10.0);
                            appliedDiscounts.add("Bulk order (-10%)");
                        }))
                        .build())

                .rule(Rule.builder()
                        .name("couponRule", "Applies a coupon code, if one was supplied and it is valid.")
                        // No coupon supplied -> the rule is SKIPPED (not failed).
                        .preCondition(condition((String coupon) -> coupon != null))
                        .given(condition((String coupon) -> lookupCoupon(coupon) != null))
                        .then(action((Bindings bindings, Double discountPercent, List<String> appliedDiscounts, String coupon) -> {
                            double percent = lookupCoupon(coupon);
                            bindings.setValue("discountPercent", discountPercent + percent);
                            appliedDiscounts.add("Coupon " + coupon + " (-" + (int) percent + "%)");
                        }))
                        // Condition failed -> the "otherwise" action runs instead.
                        .otherwise(action((List<String> appliedDiscounts, String coupon) ->
                                appliedDiscounts.add("Coupon " + coupon + " is not valid (ignored)")))
                        .build())

                // Checked after each rule: once the cap is reached, later rules never run.
                .stopCondition(condition((Double discountPercent) -> discountPercent >= MAX_DISCOUNT_PERCENT))

                // Always runs after the rules: compute the final price.
                .finalizer(action((Bindings bindings, Double subtotal, Double discountPercent) ->
                        bindings.setValue("total", subtotal * (1.0 - discountPercent / 100.0))))

                // Shape the Bindings into the caller's return value. "ruleSetStatus" is
                // a reserved binding holding the per-rule results of this run.
                .resultExtractor(function((RuleSetExecutionStatus ruleSetStatus, Double subtotal, Double discountPercent,
                                           List<String> appliedDiscounts, Double total) ->
                        new PricingSummary(subtotal, discountPercent, appliedDiscounts, total,
                                ruleSetStatus.getRuleResults(r -> true).stream()
                                        .map(r -> r.rule().getName() + "=" + r.status())
                                        .toList())))

                // A rule blew up: the exception is bound as "ex"; return a safe fallback.
                .errorHandler(function((Exception ex, Double subtotal) ->
                        new PricingSummary(subtotal, 0.0,
                                List.of("Pricing unavailable (" + rootCause(ex).getMessage() + ") - no discounts applied"),
                                subtotal, List.of())))

                .build();
    }

    /**
     * Stand-in for a coupon service. Returns the discount percent, or null when
     * the code is unknown. Throws when the (pretend) service is down.
     */
    static Double lookupCoupon(String code) {
        if ("CRASH".equals(code)) {
            throw new IllegalStateException("coupon service timed out");
        }
        return COUPONS.get(code);
    }

    private static Throwable rootCause(Throwable t) {
        while (t.getCause() != null) t = t.getCause();
        return t;
    }
}
