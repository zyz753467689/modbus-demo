package com.modbus.demo.serial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class VirtualPortManager {

    private static final Logger log = LoggerFactory.getLogger(VirtualPortManager.class);

    private static final boolean IS_WINDOWS = System.getProperty("os.name", "").toLowerCase().contains("win");

    private Process portProcess;
    private String actualPort0;
    private String actualPort1;

    public synchronized boolean startVirtualPorts() {
        stopVirtualPorts();

        if (IS_WINDOWS) {
            return startVirtualPortsWindows();
        } else {
            return startVirtualPortsUnix();
        }
    }

    private boolean startVirtualPortsUnix() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "socat", "-d", "-d",
                    "pty,raw,echo=0,link=/tmp/vmodbus0",
                    "pty,raw,echo=0,link=/tmp/vmodbus1"
            );
            pb.redirectErrorStream(true);
            portProcess = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(portProcess.getInputStream()));
            Pattern ptyPattern = Pattern.compile("/dev/\\S+");

            int count = 0;
            long deadline = System.currentTimeMillis() + 3000;
            while (count < 2 && System.currentTimeMillis() < deadline) {
                String line = reader.readLine();
                if (line != null) {
                    log.debug("socat: {}", line);
                    Matcher m = ptyPattern.matcher(line);
                    while (m.find()) {
                        String pty = m.group();
                        if (count == 0) {
                            actualPort0 = pty;
                        } else {
                            actualPort1 = pty;
                        }
                        count++;
                    }
                } else {
                    Thread.sleep(100);
                }
            }

            if (portProcess.isAlive() && actualPort0 != null && actualPort1 != null) {
                log.info("Virtual serial ports created: {} <-> {}", actualPort0, actualPort1);
                return true;
            } else if (!portProcess.isAlive()) {
                log.error("socat process exited immediately. Install: brew install socat (macOS) / apt install socat (Linux)");
                return false;
            } else {
                actualPort0 = "/tmp/vmodbus0";
                actualPort1 = "/tmp/vmodbus1";
                log.warn("Could not detect actual /dev/ paths, using symlinks as fallback");
                return portProcess.isAlive();
            }
        } catch (IOException e) {
            log.error("Failed to start socat. Install: brew install socat (macOS) / apt install socat (Linux): {}", e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private boolean startVirtualPortsWindows() {
        // On Windows, try com0com's port installer
        // com0com creates virtual COM port pairs like COM3 <-> COM4
        try {
            // Try to find com0com's install directory
            String com0comPath = findCom0com();
            if (com0comPath == null) {
                log.error("com0com not found. Download from: https://sourceforge.net/projects/com0com/");
                return false;
            }

            // Use com0com to install a port pair
            // The command format: install PortName=COM3 PortName=COM4
            ProcessBuilder pb = new ProcessBuilder(
                    com0comPath, "install", "PortName=COM5", "PortName=COM6"
            );
            pb.redirectErrorStream(true);
            portProcess = pb.start();

            Thread.sleep(2000);

            actualPort0 = "COM5";
            actualPort1 = "COM6";
            log.info("Windows virtual serial ports created: {} <-> {}", actualPort0, actualPort1);
            return true;
        } catch (Exception e) {
            log.error("Failed to create Windows virtual ports: {}. Install com0com or use physical COM ports.", e.getMessage());
            return false;
        }
    }

    private String findCom0com() {
        // Check common install paths for com0com
        String[] paths = {
                "C:\\Program Files\\com0com\\setup.exe",
                "C:\\Program Files (x86)\\com0com\\setup.exe",
                "C:\\com0com\\setup.exe"
        };
        for (String path : paths) {
            if (new java.io.File(path).exists()) {
                return path;
            }
        }
        return null;
    }

    public synchronized void stopVirtualPorts() {
        if (portProcess != null && portProcess.isAlive()) {
            portProcess.destroyForcibly();
            portProcess = null;
            log.info("Virtual serial ports stopped");
        }
        actualPort0 = null;
        actualPort1 = null;
    }

    public synchronized boolean isRunning() {
        return portProcess != null && portProcess.isAlive();
    }

    public synchronized String getPort0() {
        return actualPort0 != null ? actualPort0 : (IS_WINDOWS ? "COM5" : "/tmp/vmodbus0");
    }

    public synchronized String getPort1() {
        return actualPort1 != null ? actualPort1 : (IS_WINDOWS ? "COM6" : "/tmp/vmodbus1");
    }

    public static boolean isWindows() {
        return IS_WINDOWS;
    }
}
