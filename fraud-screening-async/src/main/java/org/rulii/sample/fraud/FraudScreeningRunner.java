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
package org.rulii.sample.fraud;

import org.rulii.model.UnrulyException;
import org.rulii.ruleflow.RuleFlow;

/**
 * Screens four payments. Watch the elapsed time on the first one: three
 * screens totalling ~330ms of latency complete in roughly the slowest
 * screen's time, because they run in parallel.
 */
public class FraudScreeningRunner {

    private static final RuleFlow<ScreeningResult> SCREENING_FLOW =
            FraudScreeningFlow.build(new ScreeningServices());

    public FraudScreeningRunner() {
        super();
    }

    public static void main(String[] args) {
        scenario("Clean payment - all screens pass in parallel",
                new Payment("PAY-1", "alice", "BOOKS-R-US", 120.00));

        scenario("Risky payment - low score + high velocity + large amount",
                new Payment("PAY-2", "bob", "BOOKS-R-US", 6_200.00));

        scenario("Watchlisted merchant",
                new Payment("PAY-3", "carol", "SHADY-IMPORTS", 250.00));

        scenario("Velocity service outage - fails open, payment still screened",
                new Payment("PAY-4", "vel-outage", "BOOKS-R-US", 80.00));

        scenario("Credit bureau hangs - awaitAll times out after 2s",
                new Payment("PAY-5", "slow-credit", "BOOKS-R-US", 60.00));

        // The async pool's worker threads are non-daemon; exit explicitly
        // instead of waiting for the hung credit-bureau call to finish.
        System.exit(0);
    }

    private static void scenario(String title, Payment p) {
        System.out.println();
        System.out.println("=== " + title);
        long start = System.currentTimeMillis();
        try {
            // The lambda parameter name is the binding name: payment -> p
            ScreeningResult result = SCREENING_FLOW.run(payment -> p);
            result.print();
        } catch (UnrulyException e) {
            System.out.println("  Screening did not complete: " + e.getMessage());
        }
        System.out.printf("  Elapsed  : %dms%n", System.currentTimeMillis() - start);
    }
}