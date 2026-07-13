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
package org.rulii.sample.signup;

import org.rulii.rule.Rule;
import org.rulii.ruleset.RuleSet;
import org.rulii.ruleset.RuleSetExecutionStatus;
import org.rulii.validation.Severity;

import java.util.Set;

import static org.rulii.validation.rules.Validators.*;

/**
 * The signup validation RuleSet — a mix of built-in validators (via the
 * Validators entry point), a reusable custom validator (PasswordStrengthRule)
 * and a cross-field annotated rule (PasswordsMatchRule).
 *
 * binding("email") means: fetch the value of the Binding named "email" at
 * execution time, and report violations against that name.
 */
public final class SignupValidationRules {

    private SignupValidationRules() {
        super();
    }

    public static RuleSet<RuleSetExecutionStatus> build() {
        return RuleSet.builder()
                .with("signupRules", "Validates a user registration form.")

                // --- email: built-in validators with their default messages
                .rule(notNull(binding("email")).build())
                .rule(email(binding("email")).build())

                // --- username: multiple validators can target one field
                .rule(notBlank(binding("username")).build())
                .rule(size(binding("username"), 3, 15).build())
                .rule(alphaNumeric(binding("username")).build())

                // --- password: our own reusable validator + a cross-field rule
                .rule(PasswordStrengthRule.builder(binding("password")).build())
                .rule(Rule.builder().build(PasswordsMatchRule.class))

                // --- age: overriding error code and message on a built-in
                .rule(notNull(binding("age")).build())
                .rule(min(binding("age"), 13)
                        .errorCode("signup.age.tooYoung")
                        .message("You must be at least 13 years old to sign up.")
                        .build())
                // Severity below ERROR does not block the signup - it is advisory.
                .rule(min(binding("age"), 18)
                        .errorCode("signup.age.minor")
                        .severity(Severity.WARNING)
                        .message("Applicant is under 18 - parental consent will be required.")
                        .build())

                // --- country / referral code
                .rule(in(binding("country"), Set.of("US", "CA", "GB", "AU")).build())
                .rule(pattern(binding("referralCode"), "REF-\\d{4}")
                        .errorCode("signup.referral.invalid")
                        .message("Referral code {0} is not valid (expected format: REF-1234).")
                        .build())

                // Creates the "ruleViolations" binding if the caller didn't supply
                // one, and throws ValidationException when any ERROR/FATAL
                // violations were collected by the end of the run.
                .validating()
                .build();
    }
}
