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

import jdk.jfr.Description;
import org.rulii.annotation.Given;
import org.rulii.annotation.Otherwise;
import org.rulii.annotation.PreCondition;
import org.rulii.annotation.Rule;
import org.rulii.validation.RuleViolation;
import org.rulii.validation.RuleViolations;

import java.util.Date;

@Rule
@Description("This Rule will validate that the first date is before the second.")
public class ConsistentDateRule {

    public ConsistentDateRule() {
        super();
    }

    @PreCondition
    public boolean preCondition(Date date1, Date date2) {
        return date1 != null && date2 != null;
    }

    @Given // Condition
    public boolean given(Date date1, Date date2) {
        return date1.compareTo(date2) < 0;
    }

    @Otherwise() // Else Action
    public void otherwise(Date date1, Date date2, RuleViolations violations) {
        violations.add(RuleViolation.builder().build("ConsistentDateRule", "Error.100",
                "Date1 [" + date1 + "] should be before Date 2[" + date2 + "]"));
    }
}
