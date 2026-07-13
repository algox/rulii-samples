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
 * The outcome of running a claim through the flow.
 */
public record ClaimDecision(String claimId, Status status, double payout, List<String> notes) {

    public enum Status {APPROVED, REJECTED, MANUAL_REVIEW}

    public static ClaimDecision approved(Claim claim, double payout, List<String> notes) {
        return new ClaimDecision(claim.getClaimId(), Status.APPROVED, payout, List.copyOf(notes));
    }

    public static ClaimDecision rejected(Claim claim, List<String> notes) {
        return new ClaimDecision(claim.getClaimId(), Status.REJECTED, 0.0, List.copyOf(notes));
    }

    public static ClaimDecision manualReview(Claim claim, List<String> notes) {
        return new ClaimDecision(claim.getClaimId(), Status.MANUAL_REVIEW, 0.0, List.copyOf(notes));
    }

    public void print() {
        System.out.printf("  Decision : %s%n", status);
        if (status == Status.APPROVED) {
            System.out.printf("  Payout   : $%,.2f%n", payout);
        }
        notes.forEach(n -> System.out.println("  Note     : " + n));
    }
}