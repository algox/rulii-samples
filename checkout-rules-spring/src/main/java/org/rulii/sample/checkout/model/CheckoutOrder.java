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
package org.rulii.sample.checkout.model;

/**
 * A checkout request. Its properties become the Bindings the XML rules
 * reference: email, total, itemCount, destinationCountry, expedited.
 */
public class CheckoutOrder {

    private final String email;
    private final double total;
    private final int itemCount;
    private final String destinationCountry;
    private final boolean expedited;

    public CheckoutOrder(String email, double total, int itemCount, String destinationCountry, boolean expedited) {
        super();
        this.email = email;
        this.total = total;
        this.itemCount = itemCount;
        this.destinationCountry = destinationCountry;
        this.expedited = expedited;
    }

    public String getEmail() {
        return email;
    }

    public double getTotal() {
        return total;
    }

    public int getItemCount() {
        return itemCount;
    }

    public String getDestinationCountry() {
        return destinationCountry;
    }

    public boolean isExpedited() {
        return expedited;
    }
}