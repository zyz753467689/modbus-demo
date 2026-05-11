package com.modbus.demo.controller;

import com.modbus.demo.modbus.server.ModbusDataStore;
import com.modbus.demo.modbus.server.ModbusTcpServer;
import com.modbus.demo.model.request.ServerStartRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/server/tcp")
public class TcpServerController {

    private final ModbusTcpServer tcpServer;

    @Value("${modbus.tcp.server.default-port:5020}")
    private int defaultPort;

    @Value("${modbus.tcp.server.default-unit-id:1}")
    private int defaultUnitId;

    @Value("${modbus.data-store.register-count:100}")
    private int registerCount;

    @Value("${modbus.data-store.coil-count:100}")
    private int coilCount;

    public TcpServerController(ModbusTcpServer tcpServer) {
        this.tcpServer = tcpServer;
    }

    @PostMapping("/start")
    public ResponseEntity<?> start(@RequestBody(required = false) ServerStartRequest request) {
        try {
            int port = request != null && request.getPort() != null ? request.getPort() : defaultPort;
            int unitId = request != null && request.getUnitId() != null ? request.getUnitId() : defaultUnitId;
            tcpServer.start(port, unitId, registerCount, coilCount);
            return ResponseEntity.ok(Map.of("message", "TCP Server started", "port", port, "unitId", unitId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/stop")
    public ResponseEntity<?> stop() {
        tcpServer.stop();
        return ResponseEntity.ok(Map.of("message", "TCP Server stopped"));
    }

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("running", tcpServer.isRunning());
        if (tcpServer.isRunning()) {
            status.put("port", tcpServer.getPort());
            status.put("unitId", tcpServer.getUnitId());
        }
        return ResponseEntity.ok(status);
    }

    @GetMapping("/data")
    public ResponseEntity<?> getData() {
        ModbusDataStore ds = tcpServer.getDataStore();
        if (ds == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Server not running"));
        }
        return ResponseEntity.ok(ds.snapshot());
    }

    @PutMapping("/data/{type}/{offset}")
    public ResponseEntity<?> setData(@PathVariable String type, @PathVariable int offset,
                                     @RequestBody Map<String, Object> body) {
        ModbusDataStore ds = tcpServer.getDataStore();
        if (ds == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Server not running"));
        }

        Object value = body.get("value");
        try {
            switch (type.toLowerCase()) {
                case "coil" -> ds.setCoil(offset, (Boolean) value);
                case "discreteinput" -> ds.setDiscreteInput(offset, (Boolean) value);
                case "holdingregister" -> ds.setHoldingRegister(offset, ((Number) value).intValue());
                case "inputregister" -> ds.setInputRegister(offset, ((Number) value).intValue());
                default -> {
                    return ResponseEntity.badRequest().body(Map.of("error", "Unknown data type: " + type));
                }
            }
            return ResponseEntity.ok(Map.of("message", "Value updated", "type", type, "offset", offset, "value", value));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
