package com.modbus.demo.controller;

import com.modbus.demo.serial.SerialPortService;
import com.modbus.demo.serial.VirtualPortManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
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
                    "error", VirtualPortManager.isWindows()
                            ? "Failed to create virtual ports. Install com0com: https://sourceforge.net/projects/com0com/"
                            : "Failed to create virtual ports. Install socat: brew install socat (macOS) / apt install socat (Linux)"
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
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("running", virtualPortManager.isRunning());
        if (virtualPortManager.isRunning()) {
            result.put("port0", virtualPortManager.getPort0());
            result.put("port1", virtualPortManager.getPort1());
        }
        return ResponseEntity.ok(result);
    }
}
