package org.dbos.apiary.etldemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class})
public class ETLDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(ETLDemoApplication.class, args);
    }
}