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
package org.openhab.core.io.transport.modbus.internal.pooling;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.ghgande.j2mod.modbus.net.SerialConnection;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import com.ghgande.j2mod.modbus.net.UDPMasterConnection;

/**
 * Adaptation layer for jamod -> j2mode compatibility; it implements the missing base class ModbusSlaveConnection
 *
 * @author Holger Friedrich - Initial contribution
 *
 */
@NonNullByDefault
public class ModbusSlaveConnection {
    @Nullable
    private SerialConnection serialConnection;
    @Nullable
    private TCPMasterConnection tcpMasterConnection;
    @Nullable
    private UDPMasterConnection udpMasterConnection;

    public ModbusSlaveConnection(SerialConnection connection) {
        this.serialConnection = connection;
    }

    public ModbusSlaveConnection(TCPMasterConnection connection) {
        this.tcpMasterConnection = connection;
    }

    public ModbusSlaveConnection(UDPMasterConnection connection) {
        this.udpMasterConnection = connection;
    }

    /**
     * Connects the connection to the endpoint
     *
     * @return whether connection was successful
     * @throws Exception on any connection errors
     */
    public boolean connect() throws Exception {
        if (serialConnection != null) {
            serialConnection.open();
            return serialConnection.isOpen();
        } else if (tcpMasterConnection != null) {
            tcpMasterConnection.connect();
            return tcpMasterConnection.isConnected();
        } else if (udpMasterConnection != null) {
            udpMasterConnection.connect();
            return udpMasterConnection.isConnected();
        }
        return false;
    }

    /**
     * Close connection and free associated resources
     */
    public void resetConnection() {
        if (serialConnection != null) {
            serialConnection.close();
        } else if (tcpMasterConnection != null) {
            tcpMasterConnection.close();
        } else if (udpMasterConnection != null) {
            udpMasterConnection.close();
        }
    }

    /**
     *
     * @return whether connection is now fully connected
     */
    public boolean isConnected() {
        if (serialConnection != null) {
            return serialConnection.isOpen();
        } else if (tcpMasterConnection != null) {
            return tcpMasterConnection.isConnected();
        } else if (udpMasterConnection != null) {
            return udpMasterConnection.isConnected();
        }
        return false;
    }

    @Nullable
    public SerialConnection toSerialConnection() {
        return serialConnection;
    }

    @Nullable
    public TCPMasterConnection toTCPMasterConnection() {
        return tcpMasterConnection;
    }

    @Nullable
    public UDPMasterConnection toUDPMasterConnection() {
        return udpMasterConnection;
    }
}
