package com.modbus.demo.poll;

import com.modbus.demo.modbus.common.ModbusFunction;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;

public class PollTask {
    private final String id;
    private final String connectionType;
    private final int unitId;
    private final ModbusFunction function;
    private final int offset;
    private final int quantity;
    private final int intervalMs;

    private ScheduledFuture<?> future;
    private Map<String, Object> lastResult;
    private long lastSuccessTime;
    private String lastError;

    public PollTask(String id, String connectionType, int unitId, ModbusFunction function,
                    int offset, int quantity, int intervalMs) {
        this.id = id;
        this.connectionType = connectionType;
        this.unitId = unitId;
        this.function = function;
        this.offset = offset;
        this.quantity = quantity;
        this.intervalMs = intervalMs;
    }

    public String getId() { return id; }
    public String getConnectionType() { return connectionType; }
    public int getUnitId() { return unitId; }
    public ModbusFunction getFunction() { return function; }
    public int getOffset() { return offset; }
    public int getQuantity() { return quantity; }
    public int getIntervalMs() { return intervalMs; }

    public ScheduledFuture<?> getFuture() { return future; }
    public void setFuture(ScheduledFuture<?> future) { this.future = future; }
    public Map<String, Object> getLastResult() { return lastResult; }
    public void setLastResult(Map<String, Object> lastResult) { this.lastResult = lastResult; }
    public long getLastSuccessTime() { return lastSuccessTime; }
    public void setLastSuccessTime(long lastSuccessTime) { this.lastSuccessTime = lastSuccessTime; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
}
