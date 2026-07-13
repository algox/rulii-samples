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

import java.util.List;

/**
 * The outcome of screening one payment.
 */
public record ScreeningResult(String paymentId, Decision decision, int riskPoints, List<String> flags) {

    public enum Decision {APPROVE, REVIEW, BLOCK}

    public void print() {
        System.out.printf("  Decision : %s (risk points: %d)%n", decision, riskPoints);
        flags.forEach(f -> System.out.println("  Flag     : " + f));
    }
}