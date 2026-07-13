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
package org.rulii.sample.checkout.service;

import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.context.RuleContextOptions;
import org.rulii.registry.RuleRegistry;
import org.rulii.rule.Rule;
import org.rulii.ruleset.RuleSet;
import org.rulii.sample.checkout.model.CheckoutOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * The only Java in the whole rule story: bind the order, run the two
 * XML-declared rulesets, read the result. Every business decision lives
 * in rules/checkout-rules.xml.
 */
@Service
public class CheckoutService {

    /** Wires Spring's converters, message resolver (MessageSource) and object factory into rule runs. */
    @Autowired
    private RuleContextOptions ruleContextOptions;
    /** XML-declared rulesets are Spring beans - injected by name. */
    @Autowired
    private RuleSet<?> orderValidationRules;
    @Autowired
    private RuleSet<?> shippingRules;
    /** Looks rules up in the application context. */
    @Autowired
    private RuleRegistry ruleRegistry;

    public CheckoutService() {
        super();
    }

    /**
     * Validates the order (throws ValidationException with localized
     * messages) and computes the shipping cost.
     */
    public ShippingQuote quote(CheckoutOrder order) {
        Bindings bindings = Bindings.builder().standard();
        bindings.loadProperties(order);
        bindings.bind("shippingCost", 0.0);
        bindings.bind("notes", new ArrayList<String>());

        RuleContext context = RuleContext.builder()
                .with(ruleContextOptions)
                .bindings(bindings)
                .build();

        orderValidationRules.run(context);
        shippingRules.run(context);

        // Profile-gated rule: only present when the "holiday" profile is
        // active. getRule returns null when absent (rulii 2.0 contract).
        Rule holidayRule = ruleRegistry.getRule("holidaySeasonRule");
        if (holidayRule != null) {
            holidayRule.run(context);
        }

        return new ShippingQuote((Double) bindings.getValue("shippingCost"),
                List.copyOf((List<?>) bindings.getValue("notes")), holidayRule != null);
    }

    public record ShippingQuote(double shippingCost, List<?> notes, boolean holidayPricing) {}
}