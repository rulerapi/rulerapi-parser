package com.maths22.ftcmanuals;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FtcManualsApplication {

    public static void main(String[] args) {
        SpringApplication.run(FtcManualsApplication.class, args);
    }
}
