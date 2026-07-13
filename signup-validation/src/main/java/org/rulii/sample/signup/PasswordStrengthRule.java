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
import org.rulii.annotation.Rule;
import org.rulii.context.RuleContext;
import org.rulii.model.function.Function;
import org.rulii.validation.RuleViolationBuilder;
import org.rulii.validation.Severity;
import org.rulii.validation.ValueValidationRule;

import java.util.ArrayList;
import java.util.List;

/**
 * A custom validation rule, built exactly like rulii's 37 built-in ones:
 * extend ValueValidationRule, implement isValid() + getSupportedTypes(),
 * and provide a companion builder (PasswordStrengthRuleBuilder).
 *
 * Once written, it composes like any built-in validator:
 *
 *   PasswordStrengthRule.builder(binding("password")).build()
 */
@Rule
@Description("Password must be at least 8 characters and contain a letter, a digit and a special character.")
public class PasswordStrengthRule extends ValueValidationRule {

    public static final List<Class<?>> SUPPORTED_TYPES = List.of(CharSequence.class);

    public static final String ERROR_CODE       = "passwordStrengthRule.errorCode";
    public static final String DEFAULT_MESSAGE  = "Password is too weak; it {1}.";

    /**
     * Creates a new builder for this validation rule.
     *
     * @param function the function that supplies the value to validate.
     * @return a new {@link PasswordStrengthRuleBuilder}.
     */
    public static PasswordStrengthRuleBuilder builder(Function<?> function) {
        return new PasswordStrengthRuleBuilder(function);
    }

    PasswordStrengthRule(Function<?> valueFunction, String errorCode, Severity severity,
                         String errorMessage, String valueName) {
        super(valueFunction, errorCode, severity, errorMessage, DEFAULT_MESSAGE, valueName);
    }

    @Override
    protected boolean isValid(RuleContext ruleContext, Object value) {
        if (value == null) return false;
        return problemsWith(value.toString()).isEmpty();
    }

    /**
     * Adds the list of failed requirements as violation parameter {1}
     * (parameter {0} is always the value itself).
     */
    @Override
    protected void customizeViolation(RuleContext ruleContext, RuleViolationBuilder builder) {
        Object value = getValue(ruleContext);
        List<String> problems = value == null ? List.of("is required") : problemsWith(value.toString());
        builder.param("problems", String.join(", ", problems));
    }

    @Override
    public List<Class<?>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    private static List<String> problemsWith(String password) {
        List<String> problems = new ArrayList<>();
        if (password.length() < 8) problems.add("must be at least 8 characters");
        if (password.chars().noneMatch(Character::isLetter)) problems.add("must contain a letter");
        if (password.chars().noneMatch(Character::isDigit)) problems.add("must contain a digit");
        if (password.chars().allMatch(Character::isLetterOrDigit)) problems.add("must contain a special character");
        return problems;
    }
}
