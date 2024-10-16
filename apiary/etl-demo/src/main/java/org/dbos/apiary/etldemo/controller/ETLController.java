package org.dbos.apiary.etldemo.controller;

import org.springframework.stereotype.Controller;
import org.dbos.apiary.client.ApiaryWorkerClient;
import org.springframework.http.MediaType;
import org.dbos.apiary.etldemo.etl.ETLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@Controller
public class ETLController {
    ApiaryWorkerClient client;

    @Autowired
    private ETLService etlService;

    // Connect to etl.html file
    @GetMapping("/")
    public String etlPage() {
        return "/home";  
    }

    // This endpoint is called when the ETL button is clicked
    @GetMapping(value = "/start-etl", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> startETLProcess() {
        try {
            // Start the ETL process
            etlService.runETL();
            // Return success message
            return ResponseEntity.status(HttpStatus.OK).body("ETL completed!");
        } catch (Exception e) {
            // Handle error and return failure message
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred during ETL process");
        }
    }
}
