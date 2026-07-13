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

import java.util.List;

/**
 * An insurance claim submitted for processing.
 */
public class Claim {

    private final String claimId;
    private final String policyNumber;
    private final String incidentType;
    private final List<LineItem> lineItems;

    public Claim(String claimId, String policyNumber, String incidentType, List<LineItem> lineItems) {
        super();
        this.claimId = claimId;
        this.policyNumber = policyNumber;
        this.incidentType = incidentType;
        this.lineItems = lineItems;
    }

    public String getClaimId() {
        return claimId;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public String getIncidentType() {
        return incidentType;
    }

    public List<LineItem> getLineItems() {
        return lineItems;
    }
}