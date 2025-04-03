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
package org.openhab.core.model.yaml.internal;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * The {@link YamlModelWrapper} is used to store the information read from a model in the model cache.
 *
 * @author Jan N. Klug - Initial contribution
 * @author Laurent Garnier - Introduce version 2 using map instead of table
 */
@NonNullByDefault
public class YamlModelWrapper {
    private final int version;
    private final boolean readOnly;
    /**
     * Nodes as a list in version 1
     */
    private final Map<String, List<JsonNode>> nodesV1 = new ConcurrentHashMap<>();
    /**
     * Nodes as a map in version >= 2
     */
    private final Map<String, @Nullable JsonNode> nodes = new ConcurrentHashMap<>();

    public YamlModelWrapper(int version, boolean readOnly) {
        this.version = version;
        this.readOnly = readOnly;
    }

    public int getVersion() {
        return version;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Get the nodes for version 1
     *
     * @return the nodes
     */
    public Map<String, List<JsonNode>> getNodesV1() {
        return nodesV1;
    }

    /**
     * Get the nodes for version >= 2
     *
     * @return the nodes
     */
    public Map<String, @Nullable JsonNode> getNodes() {
        return nodes;
    }
}
