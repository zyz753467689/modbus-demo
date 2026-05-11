package com.modbus.demo.modbus.client;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.BitVector;
import com.modbus.demo.modbus.common.ModbusFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ModbusTcpClient {

    private static final Logger log = LoggerFactory.getLogger(ModbusTcpClient.class);

    private ModbusTCPMaster master;
    private boolean connected;
    private String host;
    private int port;

    public synchronized void connect(String host, int port) throws Exception {
        if (connected) {
            disconnect();
        }

        this.host = host;
        this.port = port;
        this.master = new ModbusTCPMaster(host, port, 5000, false);
        master.connect();
        connected = true;
        log.info("Modbus TCP Client connected to {}:{}", host, port);
    }

    public synchronized void disconnect() {
        if (master != null) {
            try {
                master.disconnect();
            } catch (Exception e) {
                log.warn("Error disconnecting TCP client: {}", e.getMessage());
            }
            master = null;
        }
        connected = false;
        log.info("Modbus TCP Client disconnected");
    }

    public synchronized boolean isConnected() {
        return connected && master != null && master.isConnected();
    }

    public synchronized String getHost() {
        return host;
    }

    public synchronized int getPort() {
        return port;
    }

    public synchronized Map<String, Object> read(int unitId, ModbusFunction function, int offset, int quantity)
            throws ModbusException {
        if (!isConnected()) {
            throw new ModbusException("TCP client not connected");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("function", function.name());
        result.put("functionCode", function.getCode());
        result.put("offset", offset);
        result.put("quantity", quantity);

        switch (function) {
            case READ_COILS -> {
                BitVector bv = master.readCoils(unitId, offset, quantity);
                List<Boolean> values = new ArrayList<>(quantity);
                for (int i = 0; i < quantity; i++) {
                    values.add(bv.getBit(i));
                }
                result.put("values", values);
            }
            case READ_DISCRETE_INPUTS -> {
                BitVector bv = master.readInputDiscretes(unitId, offset, quantity);
                List<Boolean> values = new ArrayList<>(quantity);
                for (int i = 0; i < quantity; i++) {
                    values.add(bv.getBit(i));
                }
                result.put("values", values);
            }
            case READ_HOLDING_REGISTERS -> {
                Register[] regs = master.readMultipleRegisters(unitId, offset, quantity);
                List<Integer> values = new ArrayList<>(regs.length);
                for (Register reg : regs) {
                    values.add(reg.getValue());
                }
                result.put("values", values);
            }
            case READ_INPUT_REGISTERS -> {
                InputRegister[] regs = master.readInputRegisters(unitId, offset, quantity);
                List<Integer> values = new ArrayList<>(regs.length);
                for (InputRegister reg : regs) {
                    values.add(reg.getValue());
                }
                result.put("values", values);
            }
            default -> throw new ModbusException("Not a read function: " + function);
        }

        return result;
    }

    public synchronized Map<String, Object> write(int unitId, ModbusFunction function, int offset, List<?> values)
            throws ModbusException {
        if (!isConnected()) {
            throw new ModbusException("TCP client not connected");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("function", function.name());
        result.put("functionCode", function.getCode());
        result.put("offset", offset);

        switch (function) {
            case WRITE_SINGLE_COIL -> {
                boolean value = (Boolean) values.get(0);
                boolean written = master.writeCoil(unitId, offset, value);
                result.put("writtenValue", written);
            }
            case WRITE_SINGLE_REGISTER -> {
                int value = ((Number) values.get(0)).intValue();
                int written = master.writeSingleRegister(unitId, offset, new SimpleRegister(value));
                result.put("writtenValue", written);
            }
            case WRITE_MULTIPLE_COILS -> {
                BitVector bv = new BitVector(values.size());
                for (int i = 0; i < values.size(); i++) {
                    bv.setBit(i, (Boolean) values.get(i));
                }
                master.writeMultipleCoils(unitId, offset, bv);
                result.put("writtenCount", values.size());
            }
            case WRITE_MULTIPLE_REGISTERS -> {
                Register[] regs = new Register[values.size()];
                for (int i = 0; i < values.size(); i++) {
                    regs[i] = new SimpleRegister(((Number) values.get(i)).intValue());
                }
                int written = master.writeMultipleRegisters(unitId, offset, regs);
                result.put("writtenCount", written);
            }
            default -> throw new ModbusException("Not a write function: " + function);
        }

        return result;
    }
}
