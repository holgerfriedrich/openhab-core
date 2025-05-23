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
package org.openhab.core.items.events;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.types.TimeSeries;

/**
 * The {@link ItemTimeSeriesUpdatedEvent} can be used to report item time series updates through the openHAB event bus.
 * Time series events must be created with the {@link ItemEventFactory}.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class ItemTimeSeriesUpdatedEvent extends ItemEvent {

    public static final String TYPE = ItemTimeSeriesUpdatedEvent.class.getSimpleName();

    protected final TimeSeries timeSeries;

    /**
     * Constructs a new item time series updated event.
     *
     * @param topic the topic
     * @param payload the payload
     * @param itemName the item name
     * @param timeSeries the time series
     * @param source the source, can be null
     */
    protected ItemTimeSeriesUpdatedEvent(String topic, String payload, String itemName, TimeSeries timeSeries,
            @Nullable String source) {
        super(topic, payload, itemName, source);
        this.timeSeries = timeSeries;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Gets the item time series.
     *
     * @return the item time series
     */
    public TimeSeries getTimeSeries() {
        return timeSeries;
    }

    @Override
    public String toString() {
        return String.format("Item '%s' updated timeseries with %d values.", itemName, timeSeries.size());
    }
}
