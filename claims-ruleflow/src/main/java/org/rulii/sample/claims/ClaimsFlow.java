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

import org.rulii.bind.Bindings;
import org.rulii.rule.Rule;
import org.rulii.ruleflow.RuleFlow;
import org.rulii.ruleset.RuleSet;
import org.rulii.ruleset.RuleSetExecutionStatus;
import org.rulii.sample.claims.PolicyService.PolicyNotFoundException;
import org.rulii.validation.ValidationException;

import java.util.ArrayList;
import java.util.List;

import static org.rulii.model.action.Actions.action;
import static org.rulii.model.condition.Conditions.condition;
import static org.rulii.model.function.Functions.function;
import static org.rulii.validation.rules.Validators.notBlank;
import static org.rulii.validation.rules.Validators.notEmpty;

/**
 * The claim-intake pipeline, as a single RuleFlow:
 *
 *   validate -> look up policy -> coverage checks -> price each line item
 *            -> compute payout -> decision
 *
 * Before RuleFlow, this orchestration was hand-written service code: create
 * bindings, build a context, run ruleset 1, check, run ruleset 2, try/catch
 * around each service call... The flow below replaces all of that.
 */
public final class ClaimsFlow {

    private ClaimsFlow() {
        super();
    }

    public static RuleFlow<ClaimDecision> build(PolicyService policyService) {
        return RuleFlow.builder()
                .name("claimsFlow")
                .description("Processes an insurance claim from intake to decision.")

                // Declared input - fails fast when missing.
                .param("claim", Claim.class, true)

                // Working state for the whole flow.
                .bind(rejected -> false)
                .bind(manualReview -> false)
                .bind(notes -> new ArrayList<String>())
                .bind(approvedTotal -> 0.0)
                .bind(payout -> 0.0)

                // ---- Step 1: is the claim well-formed? A ValidationException is
                // handled HERE (step-level onException) - the exception is bound
                // as "ex" inside the handler body.
                .run(claimValidationRules(), spec -> spec
                        .onException(ValidationException.class, handler -> handler
                                .execute(action((Bindings bindings, List<String> notes, ValidationException ex) -> {
                                    bindings.setValue("rejected", true);
                                    ex.getViolations().forEach(v -> notes.add("Invalid claim: " + v.getErrorMessage()));
                                }))))
                .when(condition((Boolean rejected) -> rejected), body -> body
                        .<ClaimDecision>exit(function((Claim claim, List<String> notes) ->
                                ClaimDecision.rejected(claim, notes))))

                // ---- Step 2: enrich - look up the policy; the result is bound
                // as "policy" for every later step. An unknown policy is a
                // normal business outcome, handled at the step.
                .apply(function((Claim claim) -> policyService.lookup(claim.getPolicyNumber())), spec -> spec
                        .as("policy")
                        .onException(PolicyNotFoundException.class, handler -> handler
                                .execute(action((Bindings bindings, Claim claim, List<String> notes) -> {
                                    bindings.setValue("rejected", true);
                                    notes.add("Policy " + claim.getPolicyNumber() + " does not exist");
                                }))))
                .when(condition((Boolean rejected) -> rejected), body -> body
                        .<ClaimDecision>exit(function((Claim claim, List<String> notes) ->
                                ClaimDecision.rejected(claim, notes))))

                // ---- Step 3: coverage checks (a plain RuleSet as one flow step).
                .run(coverageRules())
                .when(condition((Boolean rejected) -> rejected), body -> body
                        .<ClaimDecision>exit(function((Claim claim, List<String> notes) ->
                                ClaimDecision.rejected(claim, notes))))

                // ---- Step 4: price each line item. "item" is bound to the
                // current element on every iteration.
                .forEach(function((Claim claim) -> claim.getLineItems()), "item", body -> body
                        .run(lineItemRule()))

                // ---- Step 5: compute the payout inside a named scope. The
                // intermediate "cappedTotal" binding disappears with the scope;
                // the result is exported by writing to the pre-bound "payout".
                .scope("payoutScope", body -> body
                        .apply(function((Double approvedTotal, Policy policy) ->
                                Math.min(approvedTotal, policy.getCoverageLimit())), spec -> spec.as("cappedTotal"))
                        .execute(action((Bindings bindings, Double cappedTotal, Policy policy) ->
                                bindings.setValue("payout", Math.max(0.0, cappedTotal - policy.getDeductible())))))

                // ---- Global safety net: any unhandled failure (e.g. the policy
                // service being down) routes the claim to manual review instead
                // of crashing the intake.
                .onException(Exception.class, handler -> handler
                        .execute(action((Bindings bindings, List<String> notes, Exception ex) -> {
                            bindings.setValue("manualReview", true);
                            notes.add("Processing failed (" + rootCause(ex).getMessage() + ") - routed to manual review");
                        })))

                // Always runs, even on early exit.
                .finalizer(action((Claim claim) ->
                        System.out.println("  [flow] finalizer: claim " + claim.getClaimId() + " processed")))

                // Shape the final result.
                .<ClaimDecision>returning(function((Claim claim, Boolean manualReview, Double payout, List<String> notes) ->
                        manualReview
                                ? ClaimDecision.manualReview(claim, notes)
                                : ClaimDecision.approved(claim, payout, notes)))
                .build();
    }

    /**
     * Shape validation - built-in validators reading properties straight off
     * the claim object (any Function works as a value source, not just
     * binding("name")).
     */
    private static RuleSet<RuleSetExecutionStatus> claimValidationRules() {
        return RuleSet.builder()
                .with("claimValidationRules", "Claim must be well-formed.")
                .rule(notBlank(function((Claim claim) -> claim.getClaimId())).valueName("claimId").build())
                .rule(notBlank(function((Claim claim) -> claim.getPolicyNumber())).valueName("policyNumber").build())
                .rule(notEmpty(function((Claim claim) -> claim.getLineItems())).valueName("lineItems").build())
                .validating()
                .build();
    }

    /**
     * Business checks against the enriched policy.
     */
    private static RuleSet<RuleSetExecutionStatus> coverageRules() {
        return RuleSet.builder()
                .with("coverageRules", "Is this claim covered by the policy?")
                .rule(Rule.builder()
                        .name("policyActiveRule")
                        .given(condition((Policy policy) -> policy.isActive()))
                        .otherwise(action((Bindings bindings, Policy policy, List<String> notes) -> {
                            bindings.setValue("rejected", true);
                            notes.add("Policy " + policy.getPolicyNumber() + " is not active");
                        }))
                        .build())
                .rule(Rule.builder()
                        .name("incidentCoveredRule")
                        .given(condition((Policy policy, Claim claim) ->
                                policy.getCoveredIncidents().contains(claim.getIncidentType())))
                        .otherwise(action((Bindings bindings, Claim claim, List<String> notes) -> {
                            bindings.setValue("rejected", true);
                            notes.add("Incident type " + claim.getIncidentType() + " is not covered");
                        }))
                        .build())
                .build();
    }

    /**
     * Per-line-item pricing: amounts above the policy's per-item limit are
     * capped, not rejected.
     */
    private static Rule lineItemRule() {
        return Rule.builder()
                .name("lineItemRule")
                .given(condition((LineItem item, Policy policy) -> item.getAmount() <= policy.getPerItemLimit()))
                .then(action((Bindings bindings, LineItem item, Double approvedTotal) ->
                        bindings.setValue("approvedTotal", approvedTotal + item.getAmount())))
                .otherwise(action((Bindings bindings, LineItem item, Policy policy, Double approvedTotal, List<String> notes) -> {
                    bindings.setValue("approvedTotal", approvedTotal + policy.getPerItemLimit());
                    notes.add(item.getDescription() + " capped at the per-item limit of $" + policy.getPerItemLimit());
                }))
                .build();
    }

    private static Throwable rootCause(Throwable t) {
        while (t.getCause() != null) t = t.getCause();
        return t;
    }
}
