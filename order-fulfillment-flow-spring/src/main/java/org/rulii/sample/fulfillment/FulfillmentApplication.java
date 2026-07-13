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
package org.rulii.sample.fulfillment;

import org.rulii.sample.fulfillment.service.FulfillmentService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FulfillmentApplication {

    public FulfillmentApplication() {
        super();
    }

    public static void main(String[] args) {
        SpringApplication.run(FulfillmentApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(FulfillmentService fulfillmentService) {
        return args -> {
            fulfill(fulfillmentService, "In-stock order, $650 - insured, charged and shipped in parallel",
                    "ORD-1001", 650.00);

            fulfill(fulfillmentService, "Out-of-stock order - backordered, nothing charged",
                    "ORD-OOS", 80.00);

            fulfill(fulfillmentService, "Card declined - payment step handler fires",
                    "ORD-DECLINED", 120.00);

            fulfill(fulfillmentService, "Inventory system down - global handler routes to manual review",
                    "ORD-DOWN", 45.00);
        };
    }

    private static void fulfill(FulfillmentService service, String title, String orderId, double amount) {
        System.out.println();
        System.out.println("=== " + title);
        service.fulfill(orderId, amount).print();
    }
}