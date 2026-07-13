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

import java.util.Map;
import java.util.Set;

/**
 * Stand-ins for three slow, independent screening back ends. Each call
 * sleeps to simulate network latency and prints the thread it runs on, so
 * the parallelism is visible in the output.
 */
public class ScreeningServices {

    /**
     * Extends UnrulyException so async step handlers can match it directly.
     */
    public static class ScreeningUnavailableException extends UnrulyException {
        public ScreeningUnavailableException(String service) {
            super(service + " service unavailable");
        }
    }

    private static final Map<String, Integer> CREDIT_SCORES = Map.of("alice", 720, "bob", 510, "carol", 690);
    private static final Set<String> WATCHLISTED_MERCHANTS = Set.of("SHADY-IMPORTS");
    private static final Map<String, Integer> RECENT_TXN_COUNTS = Map.of("bob", 7, "alice", 1);

    public ScreeningServices() {
        super();
    }

    /** ~150ms. Customer "slow-credit" simulates a hung bureau call. */
    public int creditScore(String customerId) {
        if ("slow-credit".equals(customerId)) sleep(5_000);
        else sleep(150);
        log("credit-bureau");
        return CREDIT_SCORES.getOrDefault(customerId, 680);
    }

    /** ~100ms. */
    public boolean isWatchlisted(String merchant) {
        sleep(100);
        log("watchlist");
        return WATCHLISTED_MERCHANTS.contains(merchant);
    }

    /** ~80ms. Customer "vel-outage" simulates the service being down. */
    public int recentTransactionCount(String customerId) {
        sleep(80);
        if ("vel-outage".equals(customerId)) {
            throw new ScreeningUnavailableException("velocity");
        }
        log("velocity");
        return RECENT_TXN_COUNTS.getOrDefault(customerId, 0);
    }

    private static void log(String service) {
        System.out.println("  [async] " + service + " responded on " + Thread.currentThread().getName());
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}