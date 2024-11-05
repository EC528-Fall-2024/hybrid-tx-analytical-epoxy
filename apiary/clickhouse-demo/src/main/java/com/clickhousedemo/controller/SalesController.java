package com.clickhousedemo.controller;

import com.clickhousedemo.service.ClickHouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SalesController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello, World!\n";
    }

    @Autowired
    private ClickHouseService clickHouseService;

    @PostMapping("/insert")
    public ResponseEntity<String> insertData(@RequestBody Map<String, Object> data) {
        try {
            clickHouseService.createTableIfNotExists();
            clickHouseService.insertData(
                (String) data.get("date"),
                (String) data.get("product"),
                (String) data.get("category"),
                ((Number) data.get("quantity")).intValue(),
                ((Number) data.get("revenue")).doubleValue()
            );
            return ResponseEntity.ok("Data inserted successfully\n");
        } catch (SQLException e) {
            return ResponseEntity.status(500).body("Error inserting data: " + e.getMessage() + "\n");
        }
    }

    @GetMapping("/averages")
    public ResponseEntity<List<Map<String, Object>>> getAverages() {
        try {
            return ResponseEntity.ok(clickHouseService.getAverages());
        } catch (SQLException e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/revenue-contribution")
    public ResponseEntity<List<Map<String, Object>>> getRevenueContribution() {
        try {
            return ResponseEntity.ok(clickHouseService.getRevenueContribution());
        } catch (SQLException e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}