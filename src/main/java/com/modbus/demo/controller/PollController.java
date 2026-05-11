package com.modbus.demo.controller;

import com.modbus.demo.modbus.common.ModbusFunction;
import com.modbus.demo.model.request.PollStartRequest;
import com.modbus.demo.poll.PollManager;
import com.modbus.demo.poll.PollTask;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/poll")
public class PollController {

    private final PollManager pollManager;

    public PollController(PollManager pollManager) {
        this.pollManager = pollManager;
    }

    @PostMapping("/start")
    public ResponseEntity<?> start(@RequestBody PollStartRequest request) {
        try {
            String id = UUID.randomUUID().toString().substring(0, 8);
            ModbusFunction function = ModbusFunction.fromCode(request.getFunction());

            if (!function.isRead()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Poll only supports read functions"));
            }

            PollTask task = pollManager.startPoll(id, request.getConnectionType(),
                    request.getUnitId(), function, request.getOffset(),
                    request.getQuantity(), request.getIntervalMs());

            return ResponseEntity.ok(Map.of(
                    "id", task.getId(),
                    "message", "Poll task started",
                    "connectionType", task.getConnectionType(),
                    "function", task.getFunction().name(),
                    "intervalMs", task.getIntervalMs()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/stop")
    public ResponseEntity<?> stop(@RequestBody Map<String, String> body) {
        String id = body.get("id");
        if (id == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "id is required"));
        }
        pollManager.stopPoll(id);
        return ResponseEntity.ok(Map.of("message", "Poll task stopped", "id", id));
    }

    @GetMapping("/list")
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(pollManager.listPolls());
    }
}
