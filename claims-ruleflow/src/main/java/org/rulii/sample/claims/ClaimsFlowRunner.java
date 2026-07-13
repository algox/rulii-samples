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
package org.rulii.sample.claims;

import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.context.RuleContextBuilder;
import org.rulii.ruleflow.RuleFlow;
import org.rulii.trace.Tracer;

import java.util.List;

/**
 * Runs six claims through the flow, hitting every path: approval, per-item
 * capping + the coverage limit, rejection (validation, unknown policy,
 * inactive policy, uncovered incident) and the manual-review safety net.
 */
public class ClaimsFlowRunner {

    private static final RuleFlow<ClaimDecision> CLAIMS_FLOW = ClaimsFlow.build(new PolicyService());

    public ClaimsFlowRunner() {
        super();
    }

    static ClaimDecision process(Claim claim, boolean withAudit) {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("claim", claim);

        RuleContextBuilder builder = RuleContext.builder().with(bindings);
        if (withAudit) {
            Tracer tracer = Tracer.builder().build();
            tracer.addListener(new AuditListener());
            builder.traceUsing(tracer);
        }
        return CLAIMS_FLOW.run(builder.build());
    }

    public static void main(String[] args) {
        scenario("Covered accident, all items within limits (audit trail on)",
                new Claim("C-100", "P-1001", "ACCIDENT",
                        List.of(new LineItem("Bumper repair", 1_200.00),
                                new LineItem("Rental car", 300.00))), true);

        scenario("Big repair - forEach caps the oversized item at the per-item limit",
                new Claim("C-101", "P-1001", "FIRE",
                        List.of(new LineItem("Kitchen rebuild", 9_000.00),
                                new LineItem("Smoke cleanup", 1_800.00),
                                new LineItem("Hotel stay", 1_900.00))), false);

        scenario("Malformed claim - no line items (validation, step handler)",
                new Claim("C-102", "P-1001", "ACCIDENT", List.of()), false);

        scenario("Unknown policy number (business error, step handler)",
                new Claim("C-103", "P-9999", "ACCIDENT",
                        List.of(new LineItem("Windshield", 400.00))), false);

        scenario("Inactive policy (coverage rules)",
                new Claim("C-104", "P-2002", "ACCIDENT",
                        List.of(new LineItem("Fender", 800.00))), false);

        scenario("Policy service outage (global handler -> manual review)",
                new Claim("C-105", "P-CRASH", "ACCIDENT",
                        List.of(new LineItem("Door panel", 600.00))), false);
    }

    private static void scenario(String title, Claim claim, boolean withAudit) {
        System.out.println();
        System.out.println("=== " + title);
        process(claim, withAudit).print();
    }
}
