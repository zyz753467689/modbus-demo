package com.modbus.demo.poll;

import com.modbus.demo.modbus.client.ModbusTcpClient;
import com.modbus.demo.modbus.client.ModbusRtuClient;
import com.modbus.demo.modbus.common.ModbusFunction;
import com.modbus.demo.websocket.DataPushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

@Component
public class PollManager {

    private static final Logger log = LoggerFactory.getLogger(PollManager.class);

    private final ModbusTcpClient tcpClient;
    private final ModbusRtuClient rtuClient;
    private final DataPushService dataPushService;

    private final Map<String, PollTask> tasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    public PollManager(ModbusTcpClient tcpClient, ModbusRtuClient rtuClient, DataPushService dataPushService) {
        this.tcpClient = tcpClient;
        this.rtuClient = rtuClient;
        this.dataPushService = dataPushService;
    }

    public PollTask startPoll(String id, String connectionType, int unitId, ModbusFunction function,
                              int offset, int quantity, int intervalMs) {
        stopPoll(id);

        PollTask task = new PollTask(id, connectionType, unitId, function, offset, quantity, intervalMs);

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                Map<String, Object> result;
                if ("TCP".equalsIgnoreCase(connectionType)) {
                    result = tcpClient.read(unitId, function, offset, quantity);
                } else {
                    result = rtuClient.read(unitId, function, offset, quantity);
                }
                task.setLastResult(result);
                task.setLastSuccessTime(System.currentTimeMillis());
                dataPushService.pushPollData(id, result);
            } catch (Exception e) {
                task.setLastError(e.getMessage());
                log.warn("Poll task {} failed: {}", id, e.getMessage());
            }
        }, 0, intervalMs, TimeUnit.MILLISECONDS);

        task.setFuture(future);
        tasks.put(id, task);
        log.info("Poll task started: id={}, type={}, function={}, offset={}, qty={}, interval={}ms",
                id, connectionType, function, offset, quantity, intervalMs);
        return task;
    }

    public void stopPoll(String id) {
        PollTask task = tasks.remove(id);
        if (task != null && task.getFuture() != null) {
            task.getFuture().cancel(true);
            log.info("Poll task stopped: id={}", id);
        }
    }

    public List<Map<String, Object>> listPolls() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (PollTask task : tasks.values()) {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("id", task.getId());
            info.put("connectionType", task.getConnectionType());
            info.put("unitId", task.getUnitId());
            info.put("function", task.getFunction().name());
            info.put("offset", task.getOffset());
            info.put("quantity", task.getQuantity());
            info.put("intervalMs", task.getIntervalMs());
            info.put("lastSuccessTime", task.getLastSuccessTime());
            info.put("lastError", task.getLastError());
            result.add(info);
        }
        return result;
    }

    public void stopAll() {
        for (String id : new ArrayList<>(tasks.keySet())) {
            stopPoll(id);
        }
    }
}
