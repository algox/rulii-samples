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
import org.rulii.rule.Rule;

import static org.rulii.model.condition.Conditions.*;
import static org.rulii.model.action.Actions.*;

public class HelloWorldRuleRunner {

    public HelloWorldRuleRunner() {
        super();
    }

    public static void runSample1(boolean flag) {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("flag", flag);

        Rule rule = Rule.builder().build(HelloWorldRule.class);
        rule.run(bindings);
    }

    public static void runSample2(boolean check) {
        Rule rule = Rule.builder()
                .name("helloWorldRule")
                .description("Rule that prints Hello world!")
                .given(condition((Boolean flag) -> check))
                .then(action(() -> System.out.println("Functional : Hello World!")))
                .build();

        Bindings bindings = Bindings.builder().standard();
        bindings.bind("flag", check);
        rule.run(bindings);
    }
    public static void main(String[] args) {
        // Declarative style
        runSample1(true);
        runSample1(false);
        // Function style
        runSample2(true);
        runSample2(false);
    }
}
