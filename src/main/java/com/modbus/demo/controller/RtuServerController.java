package com.modbus.demo.controller;

import com.modbus.demo.modbus.server.ModbusDataStore;
import com.modbus.demo.modbus.server.ModbusRtuServer;
import com.modbus.demo.model.request.RtuServerStartRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/server/rtu")
public class RtuServerController {

    private final ModbusRtuServer rtuServer;

    @Value("${modbus.rtu.server.default-baud-rate:9600}")
    private int defaultBaudRate;

    @Value("${modbus.rtu.server.default-data-bits:8}")
    private int defaultDataBits;

    @Value("${modbus.rtu.server.default-stop-bits:1}")
    private int defaultStopBits;

    @Value("${modbus.rtu.server.default-parity:0}")
    private int defaultParity;

    @Value("${modbus.rtu.server.default-unit-id:1}")
    private int defaultUnitId;

    @Value("${modbus.data-store.register-count:100}")
    private int registerCount;

    @Value("${modbus.data-store.coil-count:100}")
    private int coilCount;

    public RtuServerController(ModbusRtuServer rtuServer) {
        this.rtuServer = rtuServer;
    }

    @PostMapping("/start")
    public ResponseEntity<?> start(@RequestBody RtuServerStartRequest request) {
        try {
            String serialPort = request.getSerialPort();
            int baudRate = request.getBaudRate() != null ? request.getBaudRate() : defaultBaudRate;
            int dataBits = request.getDataBits() != null ? request.getDataBits() : defaultDataBits;
            int stopBits = request.getStopBits() != null ? request.getStopBits() : defaultStopBits;
            int parity = request.getParity() != null ? request.getParity() : defaultParity;
            int unitId = request.getUnitId() != null ? request.getUnitId() : defaultUnitId;

            rtuServer.start(serialPort, baudRate, dataBits, stopBits, parity, unitId, registerCount, coilCount);
            return ResponseEntity.ok(Map.of("message", "RTU Server started", "serialPort", serialPort, "baudRate", baudRate, "unitId", unitId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/stop")
    public ResponseEntity<?> stop() {
        rtuServer.stop();
        return ResponseEntity.ok(Map.of("message", "RTU Server stopped"));
    }

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("running", rtuServer.isRunning());
        if (rtuServer.isRunning()) {
            status.put("serialPort", rtuServer.getSerialPort());
            status.put("baudRate", rtuServer.getBaudRate());
            status.put("unitId", rtuServer.getUnitId());
        }
        return ResponseEntity.ok(status);
    }

    @GetMapping("/data")
    public ResponseEntity<?> getData() {
        ModbusDataStore ds = rtuServer.getDataStore();
        if (ds == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Server not running"));
        }
        return ResponseEntity.ok(ds.snapshot());
    }
}
