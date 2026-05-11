package com.modbus.demo.modbus.server;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.slave.ModbusSlave;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ModbusTcpServer {

    private static final Logger log = LoggerFactory.getLogger(ModbusTcpServer.class);

    private ModbusSlave slave;
    private ModbusDataStore dataStore;
    private boolean running;
    private int port;
    private int unitId;

    public synchronized void start(int port, int unitId, int registerCount, int coilCount) throws ModbusException {
        if (running) {
            stop();
        }

        this.port = port;
        this.unitId = unitId;
        this.dataStore = new ModbusDataStore(unitId, registerCount, coilCount);

        slave = ModbusSlaveFactory.createTCPSlave(port, 5);
        slave.addProcessImage(unitId, dataStore.getProcessImage());
        slave.open();

        running = true;
        log.info("Modbus TCP Server started on port {}, unitId={}", port, unitId);
    }

    public synchronized void stop() {
        if (slave != null) {
            try {
                ModbusSlaveFactory.close(slave);
            } catch (Exception e) {
                log.warn("Error stopping TCP server: {}", e.getMessage());
            }
            slave = null;
        }
        running = false;
        dataStore = null;
        log.info("Modbus TCP Server stopped");
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized ModbusDataStore getDataStore() {
        return dataStore;
    }

    public synchronized int getPort() {
        return port;
    }

    public synchronized int getUnitId() {
        return unitId;
    }
}
