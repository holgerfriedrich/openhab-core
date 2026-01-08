package org.openhab.core.io.transport.modbus.internal.pooling;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.ghgande.j2mod.modbus.net.SerialConnection;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import com.ghgande.j2mod.modbus.net.UDPMasterConnection;

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
