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
package org.rulii.sample;

import org.rulii.bind.Bindings;
import org.rulii.ruleset.RuleSet;
import org.rulii.ruleset.RuleSetExecutionStatus;
import org.rulii.validation.rules.alphnumeric.AlphaNumericValidationRule;
import org.rulii.validation.rules.notempty.NotEmptyValidationRule;
import org.rulii.validation.rules.notnull.NotNullValidationRule;
import org.rulii.validation.rules.numeric.NumericValidationRule;
import org.rulii.validation.rules.uppercase.UpperCaseValidationRule;

public class RuleSetRunner {

    public RuleSetRunner() {
        super();
    }

    public static void runSample1() {
        RuleSet<RuleSetExecutionStatus> ruleSet = RuleSet.builder()
                .with("TestRuleSet")
                .param("a", String.class)
                .param("b", Integer.class)
                .param("c", String.class)
                .rule(new AlphaNumericValidationRule("a"))
                .rule(new NotEmptyValidationRule("a"))
                .rule(new NotNullValidationRule("b"))
                .rule(new NumericValidationRule("b"))
                .rule(new UpperCaseValidationRule("c"))
                .validating()
                .build();

        Bindings bindings = Bindings.builder().standard();
        bindings.bind("a", "abc");
        bindings.bind("b", 123);
        bindings.bind("c", "CCC");

        // This run will pass
        ruleSet.run(bindings);

        bindings.setValue("c", "ccc");
        // This will throw an exception
        ruleSet.run(bindings);
    }

    public static void main(String[] args) {
        runSample1();
    }
}
