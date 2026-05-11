package com.modbus.demo.controller;

import com.modbus.demo.serial.SerialPortService;
import com.modbus.demo.serial.VirtualPortManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/serial")
public class SerialPortController {

    private final SerialPortService serialPortService;
    private final VirtualPortManager virtualPortManager;

    public SerialPortController(SerialPortService serialPortService, VirtualPortManager virtualPortManager) {
        this.serialPortService = serialPortService;
        this.virtualPortManager = virtualPortManager;
    }

    @GetMapping("/ports")
    public ResponseEntity<?> listPorts() {
        return ResponseEntity.ok(serialPortService.listPorts());
    }

    @PostMapping("/virtual/start")
    public ResponseEntity<?> startVirtualPorts() {
        boolean success = virtualPortManager.startVirtualPorts();
        if (success) {
            return ResponseEntity.ok(Map.of(
                    "message", "Virtual serial ports created",
                    "port0", virtualPortManager.getPort0(),
                    "port1", virtualPortManager.getPort1()
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to create virtual ports. Is socat installed? (brew install socat)"
            ));
        }
    }

    @PostMapping("/virtual/stop")
    public ResponseEntity<?> stopVirtualPorts() {
        virtualPortManager.stopVirtualPorts();
        return ResponseEntity.ok(Map.of("message", "Virtual serial ports stopped"));
    }

    @GetMapping("/virtual/status")
    public ResponseEntity<?> virtualPortStatus() {
        return ResponseEntity.ok(Map.of("running", virtualPortManager.isRunning()));
    }
}
