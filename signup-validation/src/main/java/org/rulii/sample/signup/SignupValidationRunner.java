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

import org.rulii.bind.Bindings;
import org.rulii.ruleset.RuleSet;
import org.rulii.ruleset.RuleSetExecutionStatus;
import org.rulii.validation.RuleViolation;
import org.rulii.validation.RuleViolations;
import org.rulii.validation.Severity;
import org.rulii.validation.ValidationException;

/**
 * Validates three signup requests: a good one, a hopeless one (every field
 * wrong -> ValidationException with the full violation list), and one that
 * only trips a WARNING (which does not block the signup).
 */
public class SignupValidationRunner {

    private static final RuleSet<RuleSetExecutionStatus> SIGNUP_RULES = SignupValidationRules.build();

    public SignupValidationRunner() {
        super();
    }

    static void validate(SignupRequest request) {
        Bindings bindings = Bindings.builder().standard();
        bindings.loadProperties(request);
        // Bind our own violations container so we can also inspect
        // non-blocking WARNING/INFO violations after a successful run.
        RuleViolations violations = new RuleViolations();
        bindings.bind("ruleViolations", violations);

        try {
            SIGNUP_RULES.run(bindings);
            System.out.println("  Signup ACCEPTED" + (violations.hasWarnings() ? " (with warnings)" : ""));
        } catch (ValidationException e) {
            System.out.println("  Signup REJECTED - " + e.getViolations().getErrorCount(Severity.ERROR)
                    + " error(s):");
        }
        violations.getViolations().forEach(SignupValidationRunner::print);
    }

    private static void print(RuleViolation violation) {
        System.out.printf("    [%-7s] %-35s %s%n",
                violation.getSeverity(), violation.getErrorCode(), violation.getErrorMessage());
    }

    public static void main(String[] args) {
        System.out.println("=== Valid signup");
        validate(new SignupRequest("kaia@example.com", "kaia42", "s3cret!pw", "s3cret!pw",
                29, "CA", "REF-0042"));

        System.out.println();
        System.out.println("=== Everything wrong");
        validate(new SignupRequest("not-an-email", "k!", "weak", "weak2",
                11, "XX", "BOGUS"));

        System.out.println();
        System.out.println("=== Valid, but under 18 - WARNING only, signup still accepted");
        validate(new SignupRequest("riley@example.com", "riley16", "pa55word!", "pa55word!",
                16, "US", "REF-1111"));
    }
}
