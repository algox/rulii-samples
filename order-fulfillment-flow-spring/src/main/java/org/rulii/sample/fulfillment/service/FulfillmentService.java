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

import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.context.RuleContextOptions;
import org.rulii.registry.RuleRegistry;
import org.rulii.ruleflow.RuleFlow;
import org.rulii.sample.fulfillment.model.FulfillmentResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs the XML-declared fulfillment flow. The flow is fetched from the
 * RuleRegistry - flows are first-class registry citizens in 2.0.
 */
@Service
public class FulfillmentService {

    @Autowired
    private RuleContextOptions ruleContextOptions;
    @Autowired
    private RuleRegistry ruleRegistry;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private ShippingService shippingService;

    public FulfillmentService() {
        super();
    }

    public FulfillmentResult fulfill(String orderId, double amount) {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("orderId", orderId);
        bindings.bind("amount", amount);
        // Service beans the flow's SpEL expressions call directly
        // (e.g. #ctx.paymentService.charge(...)).
        bindings.bind("paymentService", paymentService);
        bindings.bind("shippingService", shippingService);
        // Pre-bound outputs: the flow's own bindings vanish with its scope,
        // so it exports results by writing into these. ("status" is also how
        // the <r:exit/> early-exit paths report - XML exit has no extractor.)
        bindings.bind("status", "NEW");
        bindings.bind("notes", new ArrayList<String>());
        bindings.bind("paymentRef", "");
        bindings.bind("trackingNumber", "");
        bindings.bind("estimatedDelivery", LocalDate.class);

        RuleContext context = RuleContext.builder()
                .with(ruleContextOptions)
                .bindings(bindings)
                .build();

        RuleFlow<?> flow = ruleRegistry.getRuleFlow("fulfillmentFlow");
        flow.run(context);
        String status = (String) bindings.getValue("status");

        @SuppressWarnings("unchecked")
        List<String> notes = (List<String>) bindings.getValue("notes");

        return new FulfillmentResult(orderId, status,
                (String) bindings.getValue("paymentRef"),
                (String) bindings.getValue("trackingNumber"),
                bindings.getValue("estimatedDelivery", LocalDate.class),
                List.copyOf(notes));
    }
}