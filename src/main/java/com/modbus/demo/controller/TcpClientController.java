package com.modbus.demo.controller;

import com.modbus.demo.modbus.client.ModbusTcpClient;
import com.modbus.demo.modbus.common.ModbusFunction;
import com.modbus.demo.model.request.ClientConnectRequest;
import com.modbus.demo.model.request.ReadRequest;
import com.modbus.demo.model.request.WriteRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/client/tcp")
public class TcpClientController {

    private final ModbusTcpClient tcpClient;

    public TcpClientController(ModbusTcpClient tcpClient) {
        this.tcpClient = tcpClient;
    }

    @PostMapping("/connect")
    public ResponseEntity<?> connect(@RequestBody ClientConnectRequest request) {
        try {
            tcpClient.connect(request.getHost(), request.getPort());
            return ResponseEntity.ok(Map.of("message", "Connected", "host", request.getHost(), "port", request.getPort()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/disconnect")
    public ResponseEntity<?> disconnect() {
        tcpClient.disconnect();
        return ResponseEntity.ok(Map.of("message", "Disconnected"));
    }

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("connected", tcpClient.isConnected());
        if (tcpClient.isConnected()) {
            status.put("host", tcpClient.getHost());
            status.put("port", tcpClient.getPort());
        }
        return ResponseEntity.ok(status);
    }

    @PostMapping("/read")
    public ResponseEntity<?> read(@RequestBody ReadRequest request) {
        try {
            ModbusFunction function = ModbusFunction.fromCode(request.getFunction());
            Map<String, Object> result = tcpClient.read(request.getUnitId(), function,
                    request.getOffset(), request.getQuantity());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/write")
    public ResponseEntity<?> write(@RequestBody WriteRequest request) {
        try {
            ModbusFunction function = ModbusFunction.fromCode(request.getFunction());
            Map<String, Object> result = tcpClient.write(request.getUnitId(), function,
                    request.getOffset(), request.getValues());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
