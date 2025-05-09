/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.items;

import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Provides some default predicates that are helpful when working with items.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public final class ItemPredicates {

    /**
     * Creates a {@link Predicate} which can be used to filter {@link Item}s for a given label.
     *
     * @param label to filter
     * @return created {@link Predicate}
     */
    public static Predicate<Item> hasLabel(String label) {
        return i -> label.equalsIgnoreCase(i.getLabel());
    }
}
