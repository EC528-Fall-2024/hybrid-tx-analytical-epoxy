package org.dbos.apiary.etldemo.etl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.StringJoiner;

public class LoadToClickHouse {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final int BATCH_SIZE = 1000; // Configurable batch size for better performance

    public static void loadData(List<Map<String, List<Object>>> transformedData,
                              String clickhouseUrl, 
                              String clickhouseUser, 
                              String clickhousePassword,
                              String tableName, 
                              String databaseName) {
        if (transformedData == null || transformedData.isEmpty()) {
            throw new IllegalArgumentException("Transformed data cannot be null or empty");
        }

        try (Connection clickhouseConn = DriverManager.getConnection(clickhouseUrl, clickhouseUser, clickhousePassword);
             Statement statement = clickhouseConn.createStatement()) {
            
            setupDatabase(statement, databaseName, tableName, transformedData.get(0));
            insertData(statement, databaseName, tableName, transformedData);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load data to ClickHouse", e);
        }
    }

    private static void setupDatabase(Statement statement, 
                                    String databaseName, 
                                    String tableName,
                                    Map<String, List<Object>> firstRow) throws SQLException {
        statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + databaseName);

        String createTableSQL = buildCreateTableSQL(databaseName, tableName, firstRow);
        statement.executeUpdate(createTableSQL);
        statement.executeUpdate("TRUNCATE TABLE " + databaseName + "." + tableName);
        
        System.out.println("Database: [" + databaseName + "], table: [" + tableName + "] checked/created.");
    }

    private static String buildCreateTableSQL(String databaseName, 
                                            String tableName,
                                            Map<String, List<Object>> firstRow) {
        StringJoiner columnDefs = new StringJoiner(", ");
        
        for (String columnName : firstRow.keySet()) {
            Object sampleValue = firstRow.get(columnName).get(0);
            String dataType = getClickHouseType(sampleValue);
            columnDefs.add(columnName + " " + dataType);
        }

        return String.format("CREATE TABLE IF NOT EXISTS %s.%s (%s) ENGINE = MergeTree() ORDER BY tuple()",
                           databaseName, tableName, columnDefs.toString());
    }

    private static void insertData(Statement statement, 
                                 String databaseName,
                                 String tableName,
                                 List<Map<String, List<Object>>> transformedData) throws SQLException {
        List<String> columnNames = new ArrayList<>(transformedData.get(0).keySet());
        StringBuilder baseInsertQuery = new StringBuilder()
            .append("INSERT INTO ")
            .append(databaseName)
            .append(".")
            .append(tableName)
            .append(" (")
            .append(String.join(", ", columnNames))
            .append(") VALUES ");

        int batchCount = 0;
        StringBuilder batchValues = new StringBuilder();

        for (Map<String, List<Object>> dataMap : transformedData) {
            List<Object> firstColumnValues = dataMap.get(columnNames.get(0));
            
            for (int row = 0; row < firstColumnValues.size(); row++) {
                if (batchCount > 0) {
                    batchValues.append(", ");
                }
                
                appendRowValues(batchValues, dataMap, columnNames, row);
                batchCount++;

                if (batchCount >= BATCH_SIZE) {
                    executeBatch(statement, baseInsertQuery.toString(), batchValues.toString());
                    batchValues = new StringBuilder();
                    batchCount = 0;
                }
            }
        }

        // Insert remaining records
        if (batchCount > 0) {
            executeBatch(statement, baseInsertQuery.toString(), batchValues.toString());
        }
    }

    private static void appendRowValues(StringBuilder builder,
                                      Map<String, List<Object>> dataMap,
                                      List<String> columnNames,
                                      int row) {
        StringJoiner values = new StringJoiner(", ", "(", ")");
        
        for (String columnName : columnNames) {
            Object value = dataMap.get(columnName).get(row);
            values.add(formatValue(value));
        }
        
        builder.append(values.toString());
    }

    private static String formatValue(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof Number) {
            return "'" + value + "'";
        }
        if (value instanceof java.sql.Timestamp || value instanceof java.util.Date) {
            return "'" + DATE_FORMAT.format(value) + "'";
        }
        return "'" + value.toString().replace("'", "\\'") + "'";
    }

    private static void executeBatch(Statement statement, 
                                   String baseQuery, 
                                   String values) throws SQLException {
        statement.execute(baseQuery + values);
    }

    private static String getClickHouseType(Object value) {
        if (value == null) return "String";
        
        if (value instanceof Integer) return "UInt32";
        if (value instanceof Long) return "UInt64";
        if (value instanceof String) return "String";
        if (value instanceof Double || value instanceof Float) return "Float64";
        if (value instanceof Boolean) return "UInt8";
        if (value instanceof java.sql.Timestamp 
            || value instanceof java.util.Date) return "DateTime";
        
        return "String";
    }
}