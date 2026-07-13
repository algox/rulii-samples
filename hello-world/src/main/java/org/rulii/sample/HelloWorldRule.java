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

import org.rulii.annotation.Description;
import org.rulii.annotation.Given;
import org.rulii.annotation.Otherwise;
import org.rulii.annotation.Rule;
import org.rulii.annotation.Then;

/**
 * A Rule written as an annotated class — the declarative style.
 *2
 * The method parameter names (ex: "visitorName") are matched against
 * Binding names at execution time; that is how a Rule receives its inputs.
 */
@Rule
@Description("Greets a visitor on their first visit.")
public class HelloWorldRule {

    public HelloWorldRule() {
        super();
    }

    @Given
    public boolean isFirstVisit(Boolean firstVisit) {
        return firstVisit;
    }

    @Then
    public void greet(String visitorName) {
        System.out.println("[class-based] Hello " + visitorName + ", welcome to rulii!");
    }

    @Otherwise
    public void welcomeBack(String visitorName) {
        System.out.println("[class-based] Welcome back " + visitorName + "!");
    }
}
