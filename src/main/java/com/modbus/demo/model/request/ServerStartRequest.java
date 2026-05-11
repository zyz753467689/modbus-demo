package com.modbus.demo.model.request;

public class ServerStartRequest {
    private Integer port;
    private Integer unitId;

    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }
    public Integer getUnitId() { return unitId; }
    public void setUnitId(Integer unitId) { this.unitId = unitId; }
}
