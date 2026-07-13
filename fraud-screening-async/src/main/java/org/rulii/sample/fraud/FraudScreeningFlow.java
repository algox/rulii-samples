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
package org.rulii.sample.fraud;

import org.rulii.bind.Bindings;
import org.rulii.rule.Rule;
import org.rulii.ruleflow.RuleFlow;
import org.rulii.sample.fraud.ScreeningServices.ScreeningUnavailableException;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.rulii.model.action.Actions.action;
import static org.rulii.model.condition.Conditions.condition;
import static org.rulii.model.function.Functions.function;

/**
 * Screens a payment by fanning out three independent checks in parallel:
 *
 *                    +-> credit bureau  (~150ms)
 *   payment  --------+-> watchlist      (~100ms)     -> awaitAll -> score -> decision
 *                    +-> velocity       (~80ms)
 *
 * Total latency is the slowest screen, not the sum. Each screen writes its
 * own pre-bound binding, so the concurrent tasks never contend.
 */
public final class FraudScreeningFlow {

    private FraudScreeningFlow() {
        super();
    }

    public static RuleFlow<ScreeningResult> build(ScreeningServices services) {
        return RuleFlow.builder()
                .name("fraudScreeningFlow")
                .description("Parallel fraud screening for a card payment.")

                .param("payment", Payment.class, true)

                // Defaults double as fail-open values when a screen is skipped.
                .bind(riskPoints -> 0)
                .bind(flags -> new CopyOnWriteArrayList<String>())
                .bind(creditScore -> 680)
                .bind(watchlistHit -> false)
                .bind(velocityCount -> 0)

                // ---- Fan out. Each asyncRun returns immediately; the
                // CompletableFuture handle is bound under the .as(...) name.
                // The two value-returning sub-flows use IMMUTABLE mode: each gets
                // its own context (over a read-only snapshot of the bindings), so
                // concurrent flows can't interleave on the shared scope stack.
                // Their results travel back through the futures.
                .asyncRun(creditScreen(services), spec -> spec
                        .as("creditFuture")
                        .withImmutableBindings()
                        // Continuation: runs as soon as the screen finishes,
                        // without blocking the flow. The screen's result is
                        // bound as "bureauScore" inside the continuation body.
                        .thenRun("bureauScore", body -> body
                                .execute(action((Integer bureauScore, Payment payment) ->
                                        System.out.println("  [async] continuation: score " + bureauScore
                                                + " received for " + payment.getPaymentId())))))

                .asyncRun(velocityScreen(services), spec -> spec
                        .as("velocityFuture")
                        .withImmutableBindings()
                        // Fires as soon as the async task fails - the payment is
                        // NOT declined just because a screen is down (fail open).
                        .onException(ScreeningUnavailableException.class, handler -> handler
                                .execute(action((List<String> flags) ->
                                        flags.add("Velocity screen unavailable - skipped (fail open)")))))

                // The watchlist screen is a plain Rule in SHARED mode (the
                // default): it writes the pre-bound "watchlistHit" directly.
                .asyncRun(watchlistScreen(services), spec -> spec
                        .as("watchlistFuture"))

                // ---- Block until all three complete (or time out).
                .awaitAll(2, TimeUnit.SECONDS, "creditFuture", "watchlistFuture", "velocityFuture")

                // Credit + velocity screens are sub-flows returning values: read
                // them off the completed futures. A future whose failure was
                // handled completes with null - the pre-bound default stays.
                // (The watchlist screen wrote its pre-bound binding directly -
                // both patterns are valid.)
                .execute(action((Bindings bindings, CompletableFuture<?> creditFuture, CompletableFuture<?> velocityFuture) -> {
                    Object score = creditFuture.getNow(null);
                    if (score != null) bindings.setValue("creditScore", score);
                    Object count = velocityFuture.getNow(null);
                    if (count != null) bindings.setValue("velocityCount", count);
                }))

                // ---- Score the risk.
                .when(condition((Integer creditScore) -> creditScore < 550), body -> body
                        .execute(action((Bindings bindings, Integer riskPoints, Integer creditScore, List<String> flags) -> {
                            bindings.setValue("riskPoints", riskPoints + 40);
                            flags.add("Low credit score (" + creditScore + ")");
                        })))
                .when(condition((Boolean watchlistHit) -> watchlistHit), body -> body
                        .execute(action((Bindings bindings, Integer riskPoints, Payment payment, List<String> flags) -> {
                            bindings.setValue("riskPoints", riskPoints + 50);
                            flags.add("Merchant " + payment.getMerchant() + " is on the watchlist");
                        })))
                .when(condition((Integer velocityCount) -> velocityCount > 5), body -> body
                        .execute(action((Bindings bindings, Integer riskPoints, Integer velocityCount, List<String> flags) -> {
                            bindings.setValue("riskPoints", riskPoints + 30);
                            flags.add("High velocity (" + velocityCount + " recent transactions)");
                        })))
                .when(condition((Payment payment) -> payment.getAmount() > 5_000), body -> body
                        .execute(action((Bindings bindings, Integer riskPoints, Payment payment, List<String> flags) -> {
                            bindings.setValue("riskPoints", riskPoints + 20);
                            flags.add(String.format("Large amount ($%,.2f)", payment.getAmount()));
                        })))

                .<ScreeningResult>returning(function((Payment payment, Integer riskPoints, List<String> flags) ->
                        new ScreeningResult(payment.getPaymentId(),
                                riskPoints >= 60 ? ScreeningResult.Decision.BLOCK
                                        : riskPoints >= 30 ? ScreeningResult.Decision.REVIEW
                                        : ScreeningResult.Decision.APPROVE,
                                riskPoints, List.copyOf(flags))))
                .build();
    }

    /**
     * The credit screen as a sub-RuleFlow returning a value - its result
     * travels back through the CompletableFuture.
     */
    private static RuleFlow<Integer> creditScreen(ScreeningServices services) {
        return RuleFlow.builder()
                .name("creditScreen")
                .apply(function((Payment payment) -> services.creditScore(payment.getCustomerId())), spec -> spec.as("score"))
                .<Integer>returning(function((Integer score) -> score))
                .build();
    }

    /**
     * The watchlist screen as a plain Rule writing its pre-bound binding.
     */
    private static Rule watchlistScreen(ScreeningServices services) {
        return Rule.builder()
                .name("watchlistScreen")
                .given(condition((Payment payment) -> services.isWatchlisted(payment.getMerchant())))
                .then(action((Bindings bindings) -> bindings.setValue("watchlistHit", true)))
                .build();
    }

    private static RuleFlow<Integer> velocityScreen(ScreeningServices services) {
        return RuleFlow.builder()
                .name("velocityScreen")
                .apply(function((Payment payment) -> services.recentTransactionCount(payment.getCustomerId())), spec -> spec.as("count"))
                .<Integer>returning(function((Integer count) -> count))
                .build();
    }
}