package com.modbus.demo.model.request;

public class ReadRequest {
    private Integer unitId;
    private Integer function;
    private Integer offset;
    private Integer quantity;

    public Integer getUnitId() { return unitId; }
    public void setUnitId(Integer unitId) { this.unitId = unitId; }
    public Integer getFunction() { return function; }
    public void setFunction(Integer function) { this.function = function; }
    public Integer getOffset() { return offset; }
    public void setOffset(Integer offset) { this.offset = offset; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
