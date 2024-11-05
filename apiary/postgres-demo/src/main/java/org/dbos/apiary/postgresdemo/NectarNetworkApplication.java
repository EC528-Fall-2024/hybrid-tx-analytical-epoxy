package org.dbos.apiary.postgresdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.dbos.apiary.postgresdemo.etl.ETLService;

@SpringBootApplication
public class NectarNetworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(NectarNetworkApplication.class, args);
    }

}
