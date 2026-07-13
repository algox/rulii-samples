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
package org.rulii.sample.promo;

import org.rulii.bind.Bindings;
import org.rulii.model.UnrulyException;
import org.rulii.model.action.Action;
import org.rulii.model.condition.Condition;
import org.rulii.model.function.Function;
import org.rulii.rule.Rule;
import org.rulii.ruleset.RuleSet;
import org.rulii.ruleset.RuleSetExecutionStatus;
import org.rulii.script.BuildScriptException;
import org.rulii.script.Script;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Marketing promotions whose logic lives in JavaScript files under
 * src/main/resources/promos/ - marketing edits the .js files, no recompile.
 *
 * rulii auto-discovers the GraalJS script processor (language name "js")
 * via the ServiceLoader the moment the GraalJS jars are on the classpath.
 * Inside a script, "ctx" is the Bindings: read ctx.order, write
 * ctx.discountPercent, call Java methods like ctx.order.getTotal().
 */
public class PromoRunner {

    private static final String LANG = "js";
    private static final List<String> PROMOS = List.of("summer-sale", "first-order", "gold-tier");

    public PromoRunner() {
        super();
    }

    /**
     * Builds one Rule per promo folder: condition.js decides eligibility,
     * action.js applies the discount.
     */
    static RuleSet<RuleSetExecutionStatus> loadPromoRules() {
        var builder = RuleSet.builder().with("promoRules", "Marketing promotions, scripted.");

        for (String promo : PROMOS) {
            builder.rule(Rule.builder()
                    .name(promo.replace('-', '_'))
                    .given(Condition.builder().build(
                            Script.builder().build(LANG, load("/promos/" + promo + "/condition.js"))))
                    .then(Action.builder().build(
                            Script.builder().build(LANG, load("/promos/" + promo + "/action.js"))))
                    .build());
        }

        return builder.build();
    }

    static void checkout(String title, Order order, int orderMonth) {
        System.out.println();
        System.out.println("=== " + title);

        Bindings bindings = Bindings.builder().standard();
        bindings.bind("order", order);
        bindings.bind("orderMonth", orderMonth);
        bindings.bind("discountPercent", 0.0);
        bindings.bind("appliedPromos", new ArrayList<String>());

        loadPromoRules().run(bindings);

        // A Function can be scripted too - the final payable amount:
        Function<Number> payable = Function.builder().build(
                Script.builder().build(LANG, "ctx.order.getTotal() * (1 - ctx.discountPercent / 100)"));
        Number toPay = payable.run(bindings);

        System.out.printf("  Order total : $%,.2f%n", order.getTotal());
        List<?> promos = (List<?>) bindings.getValue("appliedPromos");
        if (promos.isEmpty()) {
            System.out.println("  Promotions  : (none)");
        } else {
            promos.forEach(p -> System.out.println("  Promotion   : " + p));
        }
        System.out.printf("  To pay      : $%,.2f%n", toPay.doubleValue());
    }

    public static void main(String[] args) {
        // GraalJS runs in interpreter mode on a stock JVM - silence the advisory warnings.
        System.setProperty("polyglotimpl.AttachLibraryFailureAction", "ignore");
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");

        int june = 6, november = 11;

        checkout("First order in June, $80 - summer sale + welcome discount stack",
                new Order("ORD-1", 80.00, true, "NONE"), june);

        checkout("Gold member in November, $40",
                new Order("ORD-2", 40.00, false, "GOLD"), november);

        checkout("Repeat customer in November, $30 - nothing applies",
                new Order("ORD-3", 30.00, false, "NONE"), november);

        // Scripts never fail silently. A syntax error is caught when the
        // script is BUILT (before any order is processed)...
        System.out.println();
        System.out.println("=== Broken scripts fail loudly");
        try {
            Script.builder().build(LANG, "ctx.total >>> nonsense (((");
        } catch (BuildScriptException e) {
            System.out.println("  Compile time: " + e.getClass().getSimpleName() + " - bad syntax rejected at build");
        }

        // ...and a script that compiles but blows up while evaluating (here: a
        // method that doesn't exist) fails at execution, wrapped in the usual
        // UnrulyException.
        Condition broken = Condition.builder().build(
                Script.builder().build(LANG, "ctx.order.noSuchMethod() > 5"));
        try {
            broken.isTrue(order -> new Order("ORD-4", 10.00, false, "NONE"));
        } catch (UnrulyException e) {
            System.out.println("  Run time    : " + e.getClass().getSimpleName() + " - evaluation failure surfaces to the caller");
        }
    }

    private static String load(String resource) {
        try (InputStream in = PromoRunner.class.getResourceAsStream(resource)) {
            if (in == null) throw new IllegalArgumentException("Missing script resource: " + resource);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + resource, e);
        }
    }
}