package com.modbus.demo.modbus.client;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.BitVector;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import com.modbus.demo.modbus.common.ModbusFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ModbusRtuClient {

    private static final Logger log = LoggerFactory.getLogger(ModbusRtuClient.class);

    private ModbusSerialMaster master;
    private boolean connected;
    private SerialParameters serialParams;

    public synchronized void connect(String serialPort, int baudRate, int dataBits, int stopBits, int parity)
            throws Exception {
        if (connected) {
            disconnect();
        }

        serialParams = new SerialParameters();
        serialParams.setPortName(serialPort);
        serialParams.setBaudRate(baudRate);
        serialParams.setDatabits(dataBits);
        serialParams.setStopbits(stopBits);
        serialParams.setParity(parity);
        serialParams.setEncoding(Modbus.SERIAL_ENCODING_RTU);

        master = new ModbusSerialMaster(serialParams, 5000);
        master.connect();
        connected = true;
        log.info("Modbus RTU Client connected to {}, baudRate={}", serialPort, baudRate);
    }

    public synchronized void disconnect() {
        if (master != null) {
            try {
                master.disconnect();
            } catch (Exception e) {
                log.warn("Error disconnecting RTU client: {}", e.getMessage());
            }
            master = null;
        }
        connected = false;
        serialParams = null;
        log.info("Modbus RTU Client disconnected");
    }

    public synchronized boolean isConnected() {
        return connected && master != null && master.isConnected();
    }

    public synchronized String getSerialPort() {
        return serialParams != null ? serialParams.getPortName() : null;
    }

    public synchronized int getBaudRate() {
        return serialParams != null ? serialParams.getBaudRate() : 0;
    }

    public synchronized Map<String, Object> read(int unitId, ModbusFunction function, int offset, int quantity)
            throws ModbusException {
        if (!isConnected()) {
            throw new ModbusException("RTU client not connected");
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
            throw new ModbusException("RTU client not connected");
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
