package com.modbus.demo.serial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class VirtualPortManager {

    private static final Logger log = LoggerFactory.getLogger(VirtualPortManager.class);

    private Process socatProcess;

    public synchronized boolean startVirtualPorts() {
        try {
            stopVirtualPorts();
            ProcessBuilder pb = new ProcessBuilder(
                    "socat", "-d", "-d",
                    "pty,raw,echo=0,link=/tmp/vmodbus0",
                    "pty,raw,echo=0,link=/tmp/vmodbus1"
            );
            pb.redirectErrorStream(true);
            socatProcess = pb.start();

            // Wait briefly for ports to be created
            Thread.sleep(500);

            if (socatProcess.isAlive()) {
                log.info("Virtual serial ports created: /tmp/vmodbus0 <-> /tmp/vmodbus1");
                return true;
            } else {
                log.error("socat process exited immediately. Is socat installed? (brew install socat)");
                return false;
            }
        } catch (IOException e) {
            log.error("Failed to start socat. Is it installed? (brew install socat): {}", e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public synchronized void stopVirtualPorts() {
        if (socatProcess != null && socatProcess.isAlive()) {
            socatProcess.destroyForcibly();
            socatProcess = null;
            log.info("Virtual serial ports stopped");
        }
    }

    public synchronized boolean isRunning() {
        return socatProcess != null && socatProcess.isAlive();
    }

    public String getPort0() {
        return "/tmp/vmodbus0";
    }

    public String getPort1() {
        return "/tmp/vmodbus1";
    }
}
