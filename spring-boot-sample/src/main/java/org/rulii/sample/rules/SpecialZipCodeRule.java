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
import org.rulii.annotation.Rule;
import org.rulii.annotation.Then;
import org.rulii.sample.model.Applicant;
import org.rulii.sample.model.LoanDecision;

import java.util.Arrays;
import java.util.List;

@Rule
@Description("Rule to check if zipcode is special.")
public class SpecialZipCodeRule {

    private static final List<String> SPECIAL_ZIP_CODES = Arrays.asList("90210");

    public SpecialZipCodeRule() {
        super();
    }
    @Given
    public boolean isSpecialZipCode(Applicant applicant) {
        return SPECIAL_ZIP_CODES.contains(applicant.getAddress().getZipcode());
    }

    @Then
    public void then(LoanDecision decision) {
        decision.setDecision(LoanDecision.DECISION.APPROVED);
    }

}
