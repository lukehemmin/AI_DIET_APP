package com.lukehemmin.dodietapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class DoDietApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DoDietApiApplication.class, args);
    }

}
