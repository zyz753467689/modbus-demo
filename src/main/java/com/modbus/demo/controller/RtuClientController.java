package com.modbus.demo.controller;

import com.modbus.demo.modbus.client.ModbusRtuClient;
import com.modbus.demo.modbus.common.ModbusFunction;
import com.modbus.demo.model.request.ReadRequest;
import com.modbus.demo.model.request.RtuClientConnectRequest;
import com.modbus.demo.model.request.WriteRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/client/rtu")
public class RtuClientController {

    private final ModbusRtuClient rtuClient;

    public RtuClientController(ModbusRtuClient rtuClient) {
        this.rtuClient = rtuClient;
    }

    @PostMapping("/connect")
    public ResponseEntity<?> connect(@RequestBody RtuClientConnectRequest request) {
        try {
            int parity = request.getParity() != null ? request.getParity() : 0;
            int dataBits = request.getDataBits() != null ? request.getDataBits() : 8;
            int stopBits = request.getStopBits() != null ? request.getStopBits() : 1;
            rtuClient.connect(request.getSerialPort(), request.getBaudRate(),
                    dataBits, stopBits, parity);
            return ResponseEntity.ok(Map.of("message", "Connected", "serialPort", request.getSerialPort(), "baudRate", request.getBaudRate()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/disconnect")
    public ResponseEntity<?> disconnect() {
        rtuClient.disconnect();
        return ResponseEntity.ok(Map.of("message", "Disconnected"));
    }

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("connected", rtuClient.isConnected());
        if (rtuClient.isConnected()) {
            status.put("serialPort", rtuClient.getSerialPort());
            status.put("baudRate", rtuClient.getBaudRate());
            status.put("dataBits", rtuClient.getDataBits());
            status.put("stopBits", rtuClient.getStopBits());
            status.put("parity", rtuClient.getParity());
        }
        return ResponseEntity.ok(status);
    }

    @PostMapping("/read")
    public ResponseEntity<?> read(@RequestBody ReadRequest request) {
        try {
            ModbusFunction function = ModbusFunction.fromCode(request.getFunction());
            Map<String, Object> result = rtuClient.read(request.getUnitId(), function,
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
            Map<String, Object> result = rtuClient.write(request.getUnitId(), function,
                    request.getOffset(), request.getValues());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
