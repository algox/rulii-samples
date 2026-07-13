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
package org.rulii.sample.checkout;

import org.rulii.sample.checkout.model.CheckoutOrder;
import org.rulii.sample.checkout.service.CheckoutService;
import org.rulii.validation.ValidationException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CheckoutApplication {

    public CheckoutApplication() {
        super();
    }

    public static void main(String[] args) {
        SpringApplication.run(CheckoutApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(CheckoutService checkoutService) {
        return args -> {
            quote(checkoutService, "US order over the free-shipping threshold",
                    new CheckoutOrder("kaia@example.com", 120.00, 3, "US", false));

            quote(checkoutService, "Small order to Germany, expedited",
                    new CheckoutOrder("riley@example.com", 30.00, 1, "DE", true));

            quote(checkoutService, "Invalid order - blank email, empty cart, unsupported country",
                    new CheckoutOrder("", 25.00, 0, "BR", false));
        };
    }

    private static void quote(CheckoutService service, String title, CheckoutOrder order) {
        System.out.println();
        System.out.println("=== " + title);
        try {
            CheckoutService.ShippingQuote result = service.quote(order);
            System.out.printf("  Shipping cost : $%,.2f%s%n", result.shippingCost(),
                    result.holidayPricing() ? "  (holiday pricing active)" : "");
            result.notes().forEach(n -> System.out.println("  Note          : " + n));
        } catch (ValidationException e) {
            System.out.println("  Order rejected:");
            e.getViolations().forEach(v ->
                    System.out.println("    - [" + v.getErrorCode() + "] " + v.getErrorMessage()));
        }
    }
}