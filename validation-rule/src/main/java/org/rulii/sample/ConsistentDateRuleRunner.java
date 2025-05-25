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
import org.rulii.validation.RuleViolation;
import org.rulii.validation.RuleViolations;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.rulii.model.action.Actions.action;
import static org.rulii.model.condition.Conditions.condition;


/**
 * Class for running rules related to date consistency checks.
 * Provides methods to run sample rules using the ConsistentDateRule class.
 *
 * @author Max Arulananthan
 */
public class ConsistentDateRuleRunner {

    public ConsistentDateRuleRunner() {
        super();
    }

    public static void runSample1(Date date1, Date date2, RuleViolations violations) {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("date1", date1);
        bindings.bind("date2", date2);
        bindings.bind("violations", violations);
        Rule rule = Rule.builder().build(ConsistentDateRule.class);
        rule.run(bindings);
    }

    public static void runSample2(Date d1, Date d2, RuleViolations violations) {
        Rule rule = Rule.builder()
                .name("ConsistentDateRule")
                .description("This Rule will validate that the first date is before the second.")
                .preCondition(condition((Date date1, Date date2) -> date1 != null && date2 != null))
                .given(condition((Date date1, Date date2) -> date1.compareTo(date2) < 0))
                .then(action((Date date1, Date date2) -> violations.add(RuleViolation.builder().build("ConsistentDateRule", "Error.100",
                "Date1 [" + date1 + "] should be before Date 2[" + date2 + "]"))))
                .build();

        Bindings bindings = Bindings.builder().standard();
        bindings.bind("date1", d1);
        bindings.bind("date2", d2);
        bindings.bind("violations", violations);

        rule.run(bindings);
    }

    private static Date parseDate(String text) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        return formatter.parse(text);
    }

    public static void main(String[] args) throws ParseException {
        RuleViolations violations = new RuleViolations();
        runSample1(parseDate("01-01-1990"), parseDate("01-01-2000"), violations);
        System.out.println(violations);
        violations = new RuleViolations();
        runSample1(parseDate("01-01-2010"), parseDate("01-01-2000"), violations);
        System.out.println(violations);

        violations = new RuleViolations();
        runSample2(parseDate("01-01-2010"), parseDate("01-01-2000"), violations);
        System.out.println(violations);
        violations = new RuleViolations();
        runSample1(parseDate("01-01-2010"), parseDate("01-01-2000"), violations);
        System.out.println(violations);
    }


}
