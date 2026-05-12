package com.modbus.demo.modbus.server;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.slave.ModbusSlave;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ModbusRtuServer {

    private static final Logger log = LoggerFactory.getLogger(ModbusRtuServer.class);

    private ModbusSlave slave;
    private ModbusDataStore dataStore;
    private boolean running;
    private SerialParameters serialParams;
    private int unitId;

    public synchronized void start(String serialPort, int baudRate, int dataBits, int stopBits, int parity,
                                   int unitId, int registerCount, int coilCount) throws ModbusException {
        if (running) {
            stop();
        }

        this.unitId = unitId;
        this.dataStore = new ModbusDataStore(unitId, registerCount, coilCount);

        serialParams = new SerialParameters();
        serialParams.setPortName(serialPort);
        serialParams.setBaudRate(baudRate);
        serialParams.setDatabits(dataBits);
        serialParams.setStopbits(stopBits);
        serialParams.setParity(parity);
        serialParams.setEncoding(com.ghgande.j2mod.modbus.Modbus.SERIAL_ENCODING_RTU);

        slave = ModbusSlaveFactory.createSerialSlave(serialParams);
        slave.addProcessImage(unitId, dataStore.getProcessImage());
        slave.open();

        running = true;
        log.info("Modbus RTU Server started on port {}, baudRate={}, unitId={}", serialPort, baudRate, unitId);
    }

    public synchronized void stop() {
        if (slave != null) {
            try {
                ModbusSlaveFactory.close(slave);
            } catch (Exception e) {
                log.warn("Error stopping RTU server: {}", e.getMessage());
            }
            slave = null;
        }
        running = false;
        dataStore = null;
        serialParams = null;
        log.info("Modbus RTU Server stopped");
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized ModbusDataStore getDataStore() {
        return dataStore;
    }

    public synchronized String getSerialPort() {
        return serialParams != null ? serialParams.getPortName() : null;
    }

    public synchronized int getBaudRate() {
        return serialParams != null ? serialParams.getBaudRate() : 0;
    }

    public synchronized int getDataBits() {
        return serialParams != null ? serialParams.getDatabits() : 0;
    }

    public synchronized int getStopBits() {
        return serialParams != null ? serialParams.getStopbits() : 0;
    }

    public synchronized int getParity() {
        return serialParams != null ? serialParams.getParity() : 0;
    }

    public synchronized int getUnitId() {
        return unitId;
    }
}
