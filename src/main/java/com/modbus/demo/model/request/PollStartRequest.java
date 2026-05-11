package com.modbus.demo.model.request;

public class PollStartRequest {
    private String connectionType;
    private Integer unitId;
    private Integer function;
    private Integer offset;
    private Integer quantity;
    private Integer intervalMs;

    public String getConnectionType() { return connectionType; }
    public void setConnectionType(String connectionType) { this.connectionType = connectionType; }
    public Integer getUnitId() { return unitId; }
    public void setUnitId(Integer unitId) { this.unitId = unitId; }
    public Integer getFunction() { return function; }
    public void setFunction(Integer function) { this.function = function; }
    public Integer getOffset() { return offset; }
    public void setOffset(Integer offset) { this.offset = offset; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Integer getIntervalMs() { return intervalMs; }
    public void setIntervalMs(Integer intervalMs) { this.intervalMs = intervalMs; }
}
