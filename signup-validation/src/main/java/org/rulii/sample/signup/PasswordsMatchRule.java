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

import org.rulii.annotation.Description;
import org.rulii.annotation.Given;
import org.rulii.annotation.Otherwise;
import org.rulii.annotation.PreCondition;
import org.rulii.annotation.Rule;
import org.rulii.validation.RuleViolations;
import org.rulii.validation.Severity;

/**
 * The lightweight way to write a validation: a plain annotated rule that
 * adds a RuleViolation from its @Otherwise action. Use this style for
 * cross-field checks; extend ValueValidationRule (see PasswordStrengthRule)
 * for reusable single-value validators.
 */
@Rule
@Description("Password and confirmation must match.")
public class PasswordsMatchRule {

    public PasswordsMatchRule() {
        super();
    }

    @PreCondition
    public boolean bothPresent(String password, String confirmPassword) {
        return password != null && confirmPassword != null;
    }

    @Given
    public boolean matches(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }

    @Otherwise
    public void reportMismatch(RuleViolations ruleViolations) {
        ruleViolations.add("passwordsMatchRule", "signup.password.mismatch",
                Severity.ERROR, "Password and confirmation do not match.");
    }
}
