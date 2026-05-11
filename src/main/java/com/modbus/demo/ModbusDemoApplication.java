package com.modbus.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ModbusDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModbusDemoApplication.class, args);
    }
}
