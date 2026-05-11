package com.modbus.demo.websocket;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DataPushService {

    private final DataWebSocketHandler webSocketHandler;

    public DataPushService(DataWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    public void pushSimulatorData(Map<String, Object> snapshot, int tick) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("type", "simulator");
        message.put("tick", tick);
        message.put("timestamp", System.currentTimeMillis());
        message.put("data", snapshot);
        webSocketHandler.broadcast(message);
    }

    public void pushPollData(String pollId, Map<String, Object> readResult) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("type", "poll");
        message.put("pollId", pollId);
        message.put("timestamp", System.currentTimeMillis());
        message.put("data", readResult);
        webSocketHandler.broadcast(message);
    }
}
