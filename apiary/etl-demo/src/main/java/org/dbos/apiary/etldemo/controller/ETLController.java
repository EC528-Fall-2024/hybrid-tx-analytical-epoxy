package org.dbos.apiary.etldemo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.http.MediaType;
import org.dbos.apiary.etldemo.etl.ETLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import org.dbos.apiary.etldemo.clickhouse.ClickHouseService; // Clickhouse demo
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.sql.SQLException;
import org.springframework.web.bind.annotation.RequestParam;

// Epoxy
import org.dbos.apiary.postgres.PostgresConnection;
import org.dbos.apiary.utilities.ApiaryConfig;
import org.dbos.apiary.worker.ApiaryNaiveScheduler;
import org.dbos.apiary.worker.ApiaryWorker;
import org.dbos.apiary.client.ApiaryWorkerClient;

// Functions
import org.dbos.apiary.etldemo.etl.GetTableNames;

@Controller
public class ETLController {
    ApiaryWorkerClient client;

    @Autowired
    private ETLService etlService;

    public ETLController() throws SQLException {
        ApiaryConfig.captureUpdates = true;
        ApiaryConfig.captureReads = true;
        ApiaryConfig.provenancePort = 5432;  // Store provenance data in the same database.

        PostgresConnection conn = new PostgresConnection("localhost", ApiaryConfig.postgresPort, "postgres", "dbos");
        ApiaryWorker apiaryWorker = new ApiaryWorker(new ApiaryNaiveScheduler(), 4, ApiaryConfig.postgres, ApiaryConfig.provenanceDefaultAddress);
        apiaryWorker.registerConnection(ApiaryConfig.postgres, conn);
        apiaryWorker.registerFunction("GetTableNames", ApiaryConfig.postgres, GetTableNames::new);
        apiaryWorker.startServing();

        this.client = new ApiaryWorkerClient("localhost");
    }

    // Connect to etl.html file
    @GetMapping("/")
    public String etlPage() {
        return "/home";  
    }

    // This endpoint is called when the ETL button is clicked
    //@GetMapping(value = "/start-etl", produces = MediaType.TEXT_PLAIN_VALUE)
    @PostMapping(value = "/start-etl", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> startETLProcess(@RequestBody Map<String, String> request) {
        try {
            String postgresUrl = request.get("postgresUrl");
            String postgresUser = request.get("postgresUser");
            String postgresPassword = request.get("postgresPassword");
            String clickhouseUrl = request.get("clickhouseUrl");
            String clickhouseUser = request.get("clickhouseUser");
            String clickhousePassword = request.get("clickhousePassword");

            // Start the ETL process
            etlService.runETL(postgresUrl, postgresUser, postgresPassword, clickhouseUrl, clickhouseUser, clickhousePassword);
            // Return success message
            return ResponseEntity.status(HttpStatus.OK).body("ETL completed!");
        } catch (Exception e) {
            // Handle error and return failure message
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred during ETL process");
        }
    }

    // Clickhouse
    // Inject the ClickHouseService into the controller
    @Autowired
    private ClickHouseService clickHouseService;

    @GetMapping("/olap")
    public String olapPage() {
        return "olap"; 
    }

    // This endpoint is called to populate databases
    @GetMapping(value = "/get-databases", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> fetchDB() {
        try {
            // Get the list of databases
            List<String> databases = clickHouseService.getDatabases();
            // Return the list of databases
            return ResponseEntity.ok(databases);
        } catch (Exception e) {
            // Handle error and return failure message
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // This endpoint fetches tables for a specific database
    @GetMapping(value = "/get-tables", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> fetchTables(@RequestParam String database) {
        try {
            // Get the list of tables for the specified database
            List<String> tables = clickHouseService.getTables(database);
            // Return the list of tables
            return ResponseEntity.ok(tables);
        } catch (Exception e) {
            // Handle error and return failure message
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // This endpoint fetches contents of a specific table
    @GetMapping(value = "/get-table-contents", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<String>>> fetchTableContents(@RequestParam String database, @RequestParam String table) {
        try {
            // Get the contents of the specified table as a map
            // ---- TEST -----
            System.out.println("Returning table NAMESNAMESNMESNMNAMSNAMESNAMESNMESNMNAMSNAMESNAMESNMESNMNAMSNAMESNAMESNMESNMNAMSNAMESNAMESNMESNMNAMSNAMESNAMESNMESNMNAMSNAMESNAMESNMESNMNAMSNAMESNAMESNMESNMNAMSNAMESNAMESNMESNMNAMSNAMESNAMESNMESNMNAMSNAMESNAMESNMESNMNAMS");
            String[] tableNames = client.executeFunction("GetTableNames").getStringArray();
            System.out.println(tableNames);

            // ---------------
            Map<String, List<String>> tableContents = clickHouseService.getTableContents(database, table);
            // Return the map of contents
            return ResponseEntity.ok(tableContents);
        } catch (Exception e) {
            // Handle error and return failure message
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
