package com.clickhousedemo.service;

import com.clickhouse.jdbc.ClickHouseDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClickHouseService {

    @Autowired
    private ClickHouseDataSource dataSource;

    // @PostConstruct
    // public void initializeTables() throws SQLException {
    //     createSalesTable();
    // }

    public void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS sales (" +
                    "date Date, " +
                    "product String, " +
                    "category String, " +
                    "quantity Int32, " +
                    "revenue Float64" +
                    ") ENGINE = MergeTree() " +
                    "ORDER BY (date, product)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
        }
    }

    public void insertData(String date, String product, String category, int quantity, double revenue) throws SQLException {
        String sql = "INSERT INTO sales (date, product, category, quantity, revenue) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(date));
            stmt.setString(2, product);
            stmt.setString(3, category);
            stmt.setInt(4, quantity);
            stmt.setDouble(5, revenue);
            stmt.addBatch();
            stmt.executeBatch();
        }
    }

    public List<Map<String, Object>> getAverages() throws SQLException {
        String sql = "SELECT AVG(revenue) AS avg_revenue FROM sales";
        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("avg_revenue", rs.getDouble("avg_revenue"));
                result.add(row);
            }
        }
        return result;
    }

    public List<Map<String, Object>> getRevenueContribution() throws SQLException {
        String sql = "SELECT product, SUM(revenue) AS product_revenue, " +
                     "(SUM(revenue) / (SELECT SUM(revenue) FROM sales) * 100) AS revenue_percentage " +
                     "FROM sales GROUP BY product ORDER BY product_revenue DESC";
        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("product", rs.getString("product"));
                row.put("product_revenue", rs.getDouble("product_revenue"));
                row.put("revenue_percentage", rs.getDouble("revenue_percentage"));
                result.add(row);
            }
        }
        return result;
    }
}