package com.example.infspringbatch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling // 이게 있어야 스케줄러 구동됨
@EnableBatchProcessing // 이게 있어야 배치 구동됨
@SpringBootApplication
public class InfSpringBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(InfSpringBatchApplication.class, args);
    }

}
