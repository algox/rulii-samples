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
package org.rulii.sample.fulfillment.rules;

import org.rulii.annotation.Description;
import org.rulii.annotation.Given;
import org.rulii.annotation.Otherwise;
import org.rulii.annotation.Rule;
import org.rulii.annotation.Then;
import org.rulii.bind.Bindings;
import org.rulii.sample.fulfillment.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A classpath-scanned rule (discovered by @RuleScan). It is created through
 * SpringObjectFactory, so regular Spring DI works inside it.
 */
@Rule(name = "inventoryCheckRule")
@Description("Checks warehouse stock for the order.")
public class InventoryCheckRule {

    @Autowired
    private InventoryService inventoryService;

    public InventoryCheckRule() {
        super();
    }

    @Given
    public boolean hasStock(String orderId) {
        return inventoryService.isInStock(orderId);
    }

    @Then
    public void markInStock(Bindings bindings) {
        bindings.setValue("inStock", true);
    }

    @Otherwise
    public void markOutOfStock(Bindings bindings) {
        bindings.setValue("inStock", false);
    }
}