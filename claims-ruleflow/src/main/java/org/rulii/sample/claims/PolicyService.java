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

import org.rulii.model.UnrulyException;

import java.util.Map;
import java.util.Set;

/**
 * Stand-in for a real policy back end.
 */
public class PolicyService {

    /**
     * Thrown when the policy number does not exist. Extends UnrulyException so
     * it propagates through the rule pipeline unwrapped - which lets a flow
     * step's onException(PolicyNotFoundException.class, ...) match it directly.
     */
    public static class PolicyNotFoundException extends UnrulyException {
        public PolicyNotFoundException(String policyNumber) {
            super("No policy found with number [" + policyNumber + "]");
        }
    }

    private static final Map<String, Policy> POLICIES = Map.of(
            "P-1001", new Policy("P-1001", true, Set.of("ACCIDENT", "FIRE"), 2_000.00, 10_000.00, 500.00),
            "P-2002", new Policy("P-2002", false, Set.of("ACCIDENT"), 1_000.00, 5_000.00, 250.00),
            "P-3003", new Policy("P-3003", true, Set.of("THEFT"), 1_500.00, 7_500.00, 100.00));

    public PolicyService() {
        super();
    }

    public Policy lookup(String policyNumber) {
        if ("P-CRASH".equals(policyNumber)) {
            throw new IllegalStateException("policy service unavailable");
        }
        Policy policy = POLICIES.get(policyNumber);
        if (policy == null) {
            throw new PolicyNotFoundException(policyNumber);
        }
        return policy;
    }
}