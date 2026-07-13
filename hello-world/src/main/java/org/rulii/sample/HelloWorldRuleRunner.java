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
package org.rulii.sample;

import org.rulii.bind.Bindings;
import org.rulii.rule.Rule;
import org.rulii.rule.RuleResult;

import static org.rulii.model.action.Actions.action;
import static org.rulii.model.condition.Conditions.condition;

/**
 * Hello World — the same Rule written two ways, run three ways.
 *
 * A Rule is simply: if (Condition) then Action(s) [otherwise Action].
 * Inputs are supplied through named Bindings; rulii matches Binding names
 * to the parameter names of your conditions and actions.
 */
public class HelloWorldRuleRunner {

    public HelloWorldRuleRunner() {
        super();
    }

    /**
     * Style 1 — Class-based (declarative): the rule lives in its own
     * annotated class (see HelloWorldRule). Inputs come from a Bindings object.
     */
    public static void classBasedRule() {
        Rule rule = Rule.builder().build(HelloWorldRule.class);

        Bindings bindings = Bindings.builder().standard();
        bindings.bind("visitorName", "Kaia");
        bindings.bind("firstVisit", true);

        RuleResult result = rule.run(bindings);
        System.out.println("[class-based] status: " + result.status());
    }

    /**
     * Style 2 — Builder-based (functional): the whole rule is assembled
     * inline with lambdas. Inputs are passed as BindingDeclarations —
     * the lambda parameter name becomes the Binding name.
     */
    public static void builderBasedRule() {
        Rule rule = Rule.builder()
                .name("helloWorldRule", "Greets a visitor on their first visit.")
                .given(condition((Boolean firstVisit) -> firstVisit))
                .then(action((String visitorName) ->
                        System.out.println("[builder-based] Hello " + visitorName + ", welcome to rulii!")))
                .otherwise(action((String visitorName) ->
                        System.out.println("[builder-based] Welcome back " + visitorName + "!")))
                .build();

        // firstVisit = true -> the "then" action runs (status PASS)
        rule.run(visitorName -> "Kaia", firstVisit -> true);
        // firstVisit = false -> the "otherwise" action runs (status FAIL)
        rule.run(visitorName -> "Riley", firstVisit -> false);
    }

    /**
     * Bonus — any POJO or Map can supply the inputs: each JavaBean
     * property (or map entry) becomes a Binding.
     */
    public static void pojoBindings() {
        Rule rule = Rule.builder().build(HelloWorldRule.class);
        rule.run(new Visitor("Max", true));
    }

    public static class Visitor {

        private final String visitorName;
        private final boolean firstVisit;

        public Visitor(String visitorName, boolean firstVisit) {
            super();
            this.visitorName = visitorName;
            this.firstVisit = firstVisit;
        }

        public String getVisitorName() {
            return visitorName;
        }

        public boolean isFirstVisit() {
            return firstVisit;
        }
    }

    public static void main(String[] args) {
        classBasedRule();
        builderBasedRule();
        pojoBindings();
    }
}
