package com.multivendor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MultiVendorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiVendorApplication.class, args);
    }
}
