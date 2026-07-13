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

import java.util.Set;

/**
 * The insurance policy a claim is made against.
 */
public class Policy {

    private final String policyNumber;
    private final boolean active;
    private final Set<String> coveredIncidents;
    private final double perItemLimit;
    private final double coverageLimit;
    private final double deductible;

    public Policy(String policyNumber, boolean active, Set<String> coveredIncidents,
                  double perItemLimit, double coverageLimit, double deductible) {
        super();
        this.policyNumber = policyNumber;
        this.active = active;
        this.coveredIncidents = coveredIncidents;
        this.perItemLimit = perItemLimit;
        this.coverageLimit = coverageLimit;
        this.deductible = deductible;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public boolean isActive() {
        return active;
    }

    public Set<String> getCoveredIncidents() {
        return coveredIncidents;
    }

    public double getPerItemLimit() {
        return perItemLimit;
    }

    public double getCoverageLimit() {
        return coverageLimit;
    }

    public double getDeductible() {
        return deductible;
    }
}