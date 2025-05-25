/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2025, Algorithmx Inc.
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
package org.rulii.sample.rules;

import org.rulii.annotation.Description;
import org.rulii.annotation.Given;
import org.rulii.annotation.Otherwise;
import org.rulii.annotation.Rule;
import org.rulii.sample.model.Applicant;
import org.rulii.sample.model.DeclineReasons;
import org.rulii.sample.model.LoanDecision;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Rule
@Description("Check the age of the applicant.")
public class MinimumAgeRule {

    @Value("${applicant.min.age:18}")
    private int minAge;

    public MinimumAgeRule() {
        super();
    }

    @Given
    public boolean isAgeValid(Applicant applicant) {
        LocalDate today = LocalDate.now();
        long age = ChronoUnit.YEARS.between(applicant.getDateOfBirth(), today);
        return age >= minAge;
    }

    @Otherwise
    public void otherwise(LoanDecision decision) {
        decision.setDecision(LoanDecision.DECISION.DECLINED);
        decision.setDecisionReasonCode(DeclineReasons.MIN_AGE_REASON);
    }
}
