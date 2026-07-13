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
package org.rulii.sample.claims;

import org.rulii.bind.NamedScope;
import org.rulii.model.action.Action;
import org.rulii.model.function.Function;
import org.rulii.ruleflow.RuleFlow;
import org.rulii.ruleflow.RuleFlowListener;
import org.rulii.ruleflow.command.RuleFlowCommand;

/**
 * An audit trail for flow executions. Register it on a Tracer and every
 * flow run through that RuleContext reports its lifecycle here - the basis
 * for compliance logs, metrics or debugging output.
 */
public class AuditListener implements RuleFlowListener {

    public AuditListener() {
        super();
    }

    @Override
    public void onRuleFlowStart(RuleFlow<?> ruleFlow, NamedScope ruleFlowScope) {
        System.out.println("  [audit] flow '" + ruleFlow.getName() + "' started");
    }

    @Override
    public void onRuleFlowCommandExecuted(RuleFlow<?> ruleFlow, RuleFlowCommand command) {
        System.out.println("  [audit]   step " + command.getClass().getSimpleName());
    }

    @Override
    public void onRuleFlowEarlyExit(RuleFlow<?> ruleFlow, Object result) {
        System.out.println("  [audit]   early exit");
    }

    @Override
    public void onRuleFlowExceptionHandled(RuleFlow<?> ruleFlow, Exception e, boolean stepLevel) {
        System.out.println("  [audit]   handled " + e.getClass().getSimpleName()
                + " (" + (stepLevel ? "step" : "global") + " handler)");
    }

    @Override
    public void onRuleFlowFinalizer(RuleFlow<?> ruleFlow, Action finalizer) {
        System.out.println("  [audit]   finalizer ran");
    }

    @Override
    public void onRuleFlowError(RuleFlow<?> ruleFlow, Exception e) {
        System.out.println("  [audit]   ERROR " + e.getClass().getSimpleName());
    }

    @Override
    public void onRuleFlowEnd(RuleFlow<?> ruleFlow, NamedScope ruleFlowScope) {
        System.out.println("  [audit] flow '" + ruleFlow.getName() + "' ended");
    }
}
