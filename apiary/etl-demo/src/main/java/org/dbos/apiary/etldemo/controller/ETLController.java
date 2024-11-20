package org.dbos.apiary.etldemo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.http.MediaType;
import org.dbos.apiary.etldemo.etl.ETLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.dbos.apiary.etldemo.clickhouse.ClickHouseService;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.sql.SQLException;
import org.springframework.web.bind.annotation.RequestParam;
import main.java.org.dbos.apiary.etldemo.etl.PerformETL;
import org.dbos.apiary.postgres.PostgresConnection;
import org.dbos.apiary.utilities.ApiaryConfig;
import org.dbos.apiary.worker.ApiaryNaiveScheduler;
import org.dbos.apiary.worker.ApiaryWorker;
import org.dbos.apiary.client.ApiaryWorkerClient;
import org.dbos.apiary.etldemo.etl.GetTableNames;

@Controller
public class ETLController {
    ApiaryWorkerClient client;

    @Autowired
    private ETLService etlService;

    public ETLController() throws SQLException {

        // Configure Apiary settings
        ApiaryConfig.captureUpdates = true;
        ApiaryConfig.captureReads = true;
        ApiaryConfig.provenancePort = 5432;

        // Initialize PostgreSQL connection
        PostgresConnection conn = new PostgresConnection(
            "localhost", 
            ApiaryConfig.postgresPort, 
            "postgres", 
            "dbos"
        );

        // Set up Apiary worker
        ApiaryWorker apiaryWorker = new ApiaryWorker(
            new ApiaryNaiveScheduler(), 
            4, 
            ApiaryConfig.postgres, 
            ApiaryConfig.provenanceDefaultAddress
        );

        // Register connections and functions
        apiaryWorker.registerConnection(ApiaryConfig.postgres, conn);
        apiaryWorker.registerFunction("GetTableNames", ApiaryConfig.postgres, GetTableNames::new);
        apiaryWorker.startServing();

        this.client = new ApiaryWorkerClient("localhost");
    }

    @PostMapping(value = "/start-etl", consumes = MediaType.APPLICATION_JSON_VALUE)

    @GetMapping("/")
    public String etlPage() {
        return "/home";  
    
    }
    public ResponseEntity<String> startETLProcess(@RequestBody Map<String, String> request) {
        try {
            String postgresUrl = request.get("postgresUrl");
            String postgresUser = request.get("postgresUser");
            String postgresPassword = request.get("postgresPassword");
            String clickhouseUrl = request.get("clickhouseUrl");
            String clickhouseUser = request.get("clickhouseUser");
            String clickhousePassword = request.get("clickhousePassword");

            etlService.runETL(postgresUrl, postgresUser, postgresPassword, 
                            clickhouseUrl, clickhouseUser, clickhousePassword);
            
            return ResponseEntity.ok("ETL process completed successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("ETL process failed: " + e.getMessage());
        }
    }
}
