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

import org.rulii.model.function.Function;
import org.rulii.validation.Severity;
import org.rulii.validation.ValueValidationRuleBuilder;

/**
 * Fluent builder for {@link PasswordStrengthRule}. Inherits errorCode(),
 * severity(), message(), valueName(), name() and description() from
 * ValueValidationRuleBuilder; build() produces a ready-to-use Rule.
 */
public class PasswordStrengthRuleBuilder
        extends ValueValidationRuleBuilder<PasswordStrengthRuleBuilder, PasswordStrengthRule> {

    public PasswordStrengthRuleBuilder(Function<?> valueFunction) {
        super(valueFunction);
        errorCode(PasswordStrengthRule.ERROR_CODE);
        severity(Severity.ERROR);
        message(PasswordStrengthRule.DEFAULT_MESSAGE);
    }

    @Override
    protected PasswordStrengthRule createValueValidationRule() {
        return new PasswordStrengthRule(getValueFunction(), getErrorCode(), getSeverity(),
                getErrorMessage(), getValueName());
    }
}
