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
package org.openhab.core.automation.dto;

import java.util.List;

/**
 * This is a data transfer object that is used to serialize the respective class.
 *
 * @author Ana Dimova - Initial contribution
 */
public class CompositeTriggerTypeDTO extends TriggerTypeDTO {

    public List<TriggerDTO> children;
}
