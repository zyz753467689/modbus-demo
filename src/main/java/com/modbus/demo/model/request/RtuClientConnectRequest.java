package com.modbus.demo.model.request;

public class RtuClientConnectRequest {
    private String serialPort;
    private Integer baudRate;
    private Integer dataBits;
    private Integer stopBits;
    private Integer parity;

    public String getSerialPort() { return serialPort; }
    public void setSerialPort(String serialPort) { this.serialPort = serialPort; }
    public Integer getBaudRate() { return baudRate; }
    public void setBaudRate(Integer baudRate) { this.baudRate = baudRate; }
    public Integer getDataBits() { return dataBits; }
    public void setDataBits(Integer dataBits) { this.dataBits = dataBits; }
    public Integer getStopBits() { return stopBits; }
    public void setStopBits(Integer stopBits) { this.stopBits = stopBits; }
    public Integer getParity() { return parity; }
    public void setParity(Integer parity) { this.parity = parity; }
}
