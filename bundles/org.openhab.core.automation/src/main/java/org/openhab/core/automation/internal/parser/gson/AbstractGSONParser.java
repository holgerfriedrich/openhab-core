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
package org.openhab.core.automation.internal.parser.gson;

import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.automation.parser.Parser;
import org.openhab.core.automation.type.CompositeActionType;
import org.openhab.core.automation.type.CompositeConditionType;
import org.openhab.core.automation.type.CompositeTriggerType;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.core.ConfigurationDeserializer;
import org.openhab.core.config.core.ConfigurationSerializer;
import org.openhab.core.config.core.OrderingMapSerializer;
import org.openhab.core.config.core.OrderingSetSerializer;
import org.openhab.core.library.types.DateTimeType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Abstract class that can be used by the parsers for the different entity types.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Ana Dimova - add Instance Creators
 * @author Sami Salonen - add sorting for maps and sets for minimal diffs
 *
 * @param <T> the type of the entities to parse
 */
@NonNullByDefault
public abstract class AbstractGSONParser<T> implements Parser<T> {

    // A Gson instance to use by the parsers
    protected static Gson gson = new GsonBuilder() //
            .setDateFormat(DateTimeType.DATE_PATTERN_JSON_COMPAT) //
            .registerTypeAdapter(CompositeActionType.class, new ActionInstanceCreator()) //
            .registerTypeAdapter(CompositeConditionType.class, new ConditionInstanceCreator()) //
            .registerTypeAdapter(CompositeTriggerType.class, new TriggerInstanceCreator()) //
            .registerTypeAdapter(Configuration.class, new ConfigurationDeserializer()) //
            .registerTypeAdapter(Configuration.class, new ConfigurationSerializer()) //
            .registerTypeHierarchyAdapter(Map.class, new OrderingMapSerializer()) //
            .registerTypeHierarchyAdapter(Set.class, new OrderingSetSerializer()) //
            .create();

    @Override
    public void serialize(Set<T> dataObjects, OutputStreamWriter writer) throws Exception {
        gson.toJson(dataObjects, writer);
    }
}
