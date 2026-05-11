package com.modbus.demo.modbus.server;

import com.ghgande.j2mod.modbus.procimg.*;
import com.modbus.demo.modbus.common.ModbusFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ModbusDataStore {

    private static final Logger log = LoggerFactory.getLogger(ModbusDataStore.class);

    private final SimpleProcessImage processImage;
    private final int registerCount;
    private final int coilCount;

    public ModbusDataStore(int unitId, int registerCount, int coilCount) {
        this.registerCount = registerCount;
        this.coilCount = coilCount;
        this.processImage = new SimpleProcessImage(unitId);

        for (int i = 0; i < coilCount; i++) {
            processImage.addDigitalOut(i, new SimpleDigitalOut(false));
            processImage.addDigitalIn(i, new SimpleDigitalIn(false));
        }
        for (int i = 0; i < registerCount; i++) {
            processImage.addRegister(i, new SimpleRegister(0));
            processImage.addInputRegister(i, new SimpleInputRegister(0));
        }
    }

    public ProcessImage getProcessImage() {
        return processImage;
    }

    public int getUnitId() {
        return processImage.getUnitID();
    }

    // --- Coils (DigitalOut) ---

    public synchronized boolean getCoil(int offset) {
        try {
            return processImage.getDigitalOut(offset).isSet();
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized void setCoil(int offset, boolean value) {
        try {
            processImage.setDigitalOut(offset, new SimpleDigitalOut(value));
        } catch (Exception e) {
            log.warn("Failed to set coil at offset {}: {}", offset, e.getMessage());
        }
    }

    public synchronized List<Boolean> getAllCoils() {
        List<Boolean> coils = new ArrayList<>(coilCount);
        for (int i = 0; i < coilCount; i++) {
            coils.add(getCoil(i));
        }
        return coils;
    }

    // --- Discrete Inputs (DigitalIn) ---

    public synchronized boolean getDiscreteInput(int offset) {
        try {
            return processImage.getDigitalIn(offset).isSet();
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized void setDiscreteInput(int offset, boolean value) {
        try {
            processImage.setDigitalIn(offset, new SimpleDigitalIn(value));
        } catch (Exception e) {
            log.warn("Failed to set discrete input at offset {}: {}", offset, e.getMessage());
        }
    }

    public synchronized List<Boolean> getAllDiscreteInputs() {
        List<Boolean> inputs = new ArrayList<>(coilCount);
        for (int i = 0; i < coilCount; i++) {
            inputs.add(getDiscreteInput(i));
        }
        return inputs;
    }

    // --- Holding Registers ---

    public synchronized int getHoldingRegister(int offset) {
        try {
            return processImage.getRegister(offset).getValue();
        } catch (Exception e) {
            return 0;
        }
    }

    public synchronized void setHoldingRegister(int offset, int value) {
        try {
            processImage.setRegister(offset, new SimpleRegister(value));
        } catch (Exception e) {
            log.warn("Failed to set holding register at offset {}: {}", offset, e.getMessage());
        }
    }

    public synchronized List<Integer> getAllHoldingRegisters() {
        List<Integer> registers = new ArrayList<>(registerCount);
        for (int i = 0; i < registerCount; i++) {
            registers.add(getHoldingRegister(i));
        }
        return registers;
    }

    // --- Input Registers ---

    public synchronized int getInputRegister(int offset) {
        try {
            return processImage.getInputRegister(offset).getValue();
        } catch (Exception e) {
            return 0;
        }
    }

    public synchronized void setInputRegister(int offset, int value) {
        try {
            processImage.setInputRegister(offset, new SimpleInputRegister(value));
        } catch (Exception e) {
            log.warn("Failed to set input register at offset {}: {}", offset, e.getMessage());
        }
    }

    public synchronized List<Integer> getAllInputRegisters() {
        List<Integer> registers = new ArrayList<>(registerCount);
        for (int i = 0; i < registerCount; i++) {
            registers.add(getInputRegister(i));
        }
        return registers;
    }

    // --- Snapshot ---

    public synchronized Map<String, Object> snapshot() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("coils", getAllCoils());
        data.put("discreteInputs", getAllDiscreteInputs());
        data.put("holdingRegisters", getAllHoldingRegisters());
        data.put("inputRegisters", getAllInputRegisters());
        return data;
    }

    public synchronized void setValue(ModbusFunction function, int offset, Object value) {
        switch (function) {
            case WRITE_SINGLE_COIL, WRITE_MULTIPLE_COILS -> setCoil(offset, (Boolean) value);
            case WRITE_SINGLE_REGISTER, WRITE_MULTIPLE_REGISTERS -> setHoldingRegister(offset, ((Number) value).intValue());
            default -> throw new UnsupportedOperationException("Cannot write with function: " + function);
        }
    }
}
