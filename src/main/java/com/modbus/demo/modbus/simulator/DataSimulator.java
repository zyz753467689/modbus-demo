package com.modbus.demo.modbus.simulator;

import com.modbus.demo.modbus.server.ModbusDataStore;
import com.modbus.demo.modbus.server.ModbusTcpServer;
import com.modbus.demo.websocket.DataPushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Random;

@Component
public class DataSimulator {

    private static final Logger log = LoggerFactory.getLogger(DataSimulator.class);

    private final ModbusTcpServer tcpServer;
    private final DataPushService dataPushService;

    @Value("${modbus.simulator.enabled:true}")
    private boolean enabled;

    private final Random random = new Random();
    private int tick = 0;

    public DataSimulator(ModbusTcpServer tcpServer, DataPushService dataPushService) {
        this.tcpServer = tcpServer;
        this.dataPushService = dataPushService;
    }

    @Scheduled(fixedDelayString = "${modbus.simulator.interval-ms:2000}")
    public void simulate() {
        if (!enabled) {
            return;
        }

        ModbusDataStore ds = tcpServer.getDataStore();
        if (ds == null) {
            return;
        }

        tick++;

        // Counter: Holding Register 0 increments
        int counter = ds.getHoldingRegister(0);
        ds.setHoldingRegister(0, (counter + 1) % 65536);

        // Sine wave: Holding Register 1
        int sineValue = (int) (Math.sin(tick * 0.1) * 10000 + 32768);
        ds.setHoldingRegister(1, Math.max(0, Math.min(65535, sineValue)));

        // Random walk: Input Register 0
        int walk = ds.getInputRegister(0) + random.nextInt(-50, 51);
        ds.setInputRegister(0, Math.max(0, Math.min(65535, walk)));

        // Random: Input Register 1
        ds.setInputRegister(1, random.nextInt(65536));

        // Toggle: Coil 0 flips every 5 ticks
        if (tick % 5 == 0) {
            ds.setCoil(0, !ds.getCoil(0));
        }

        // Random coils
        if (tick % 3 == 0) {
            ds.setCoil(1, random.nextBoolean());
        }

        // Push data update via WebSocket
        dataPushService.pushSimulatorData(ds.snapshot(), tick);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
