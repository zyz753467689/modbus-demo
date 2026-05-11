package com.modbus.demo.modbus.common;

public enum ModbusFunction {
    READ_COILS(1, "读线圈"),
    READ_DISCRETE_INPUTS(2, "读离散输入"),
    READ_HOLDING_REGISTERS(3, "读保持寄存器"),
    READ_INPUT_REGISTERS(4, "读输入寄存器"),
    WRITE_SINGLE_COIL(5, "写单个线圈"),
    WRITE_SINGLE_REGISTER(6, "写单个寄存器"),
    WRITE_MULTIPLE_COILS(15, "写多个线圈"),
    WRITE_MULTIPLE_REGISTERS(16, "写多个寄存器");

    private final int code;
    private final String description;

    ModbusFunction(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRead() {
        return this == READ_COILS || this == READ_DISCRETE_INPUTS
                || this == READ_HOLDING_REGISTERS || this == READ_INPUT_REGISTERS;
    }

    public boolean isWrite() {
        return !isRead();
    }

    public static ModbusFunction fromCode(int code) {
        for (ModbusFunction f : values()) {
            if (f.code == code) {
                return f;
            }
        }
        throw new IllegalArgumentException("Unknown Modbus function code: " + code);
    }
}
