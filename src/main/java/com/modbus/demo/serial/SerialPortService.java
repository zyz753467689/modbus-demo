package com.modbus.demo.serial;

import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SerialPortService {

    private static final Logger log = LoggerFactory.getLogger(SerialPortService.class);

    public List<Map<String, Object>> listPorts() {
        List<Map<String, Object>> ports = new ArrayList<>();
        for (SerialPort port : SerialPort.getCommPorts()) {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("name", port.getSystemPortName());
            info.put("description", port.getDescriptivePortName());
            info.put("systemName", port.getSystemPortName());
            ports.add(info);
        }
        return ports;
    }
}
