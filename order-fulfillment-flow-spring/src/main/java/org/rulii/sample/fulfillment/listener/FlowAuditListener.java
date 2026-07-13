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
package org.rulii.sample.fulfillment.listener;

import org.rulii.bind.NamedScope;
import org.rulii.ruleflow.RuleFlow;
import org.rulii.ruleflow.RuleFlowListener;
import org.springframework.stereotype.Component;

/**
 * Any RuleFlowListener / RuleListener / RuleSetListener / RuliiListener bean
 * in the context is registered on the auto-configured Tracer automatically -
 * every flow run in the application reports here without any wiring.
 */
@Component
public class FlowAuditListener implements RuleFlowListener {

    public FlowAuditListener() {
        super();
    }

    @Override
    public void onRuleFlowStart(RuleFlow<?> ruleFlow, NamedScope ruleFlowScope) {
        System.out.println("  [audit   ] flow '" + ruleFlow.getName() + "' started");
    }

    @Override
    public void onRuleFlowExceptionHandled(RuleFlow<?> ruleFlow, Exception e, boolean stepLevel) {
        System.out.println("  [audit   ] handled " + e.getClass().getSimpleName()
                + " (" + (stepLevel ? "step" : "global") + " handler)");
    }

    @Override
    public void onRuleFlowEnd(RuleFlow<?> ruleFlow, NamedScope ruleFlowScope) {
        System.out.println("  [audit   ] flow '" + ruleFlow.getName() + "' ended");
    }
}