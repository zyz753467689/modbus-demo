package com.modbus.demo.model.request;

import java.util.List;

public class WriteRequest {
    private Integer unitId;
    private Integer function;
    private Integer offset;
    private List<Object> values;

    public Integer getUnitId() { return unitId; }
    public void setUnitId(Integer unitId) { this.unitId = unitId; }
    public Integer getFunction() { return function; }
    public void setFunction(Integer function) { this.function = function; }
    public Integer getOffset() { return offset; }
    public void setOffset(Integer offset) { this.offset = offset; }
    public List<Object> getValues() { return values; }
    public void setValues(List<Object> values) { this.values = values; }
}
