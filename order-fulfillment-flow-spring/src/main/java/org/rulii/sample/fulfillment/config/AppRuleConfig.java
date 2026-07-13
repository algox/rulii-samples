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
package org.rulii.sample.fulfillment.config;

import org.rulii.spring.annotation.RuleScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Combined discovery: classpath scan for @Rule classes AND every rulii XML
 * file under classpath:rules/.
 */
@Configuration
@RuleScan(
        scanBasePackages = "org.rulii.sample.fulfillment.rules",
        xmlLocations     = "classpath:rules/"
)
public class AppRuleConfig {

    public AppRuleConfig() {
        super();
    }

    /**
     * The async pool used by async-run steps. Auto-configuration is overridden
     * by NAME - the bean must be called "rulii.executorService"; unrelated
     * ExecutorService beans are deliberately ignored.
     */
    @Bean(name = "rulii.executorService", destroyMethod = "shutdown")
    public ExecutorService ruliiExecutorService() {
        AtomicInteger counter = new AtomicInteger();
        return Executors.newFixedThreadPool(4,
                r -> new Thread(r, "fulfillment-" + counter.incrementAndGet()));
    }

    /**
     * An explicit Clock bean: honored by rulii's ruleContextOptions and
     * injectable into rules - tests swap it for a fixed clock.
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}