// package org.dbos.apiary.etldemo.etl;

// import java.sql.Connection;
// import java.sql.DriverManager;
// import java.sql.ResultSet;
// import java.sql.Statement;
// import java.text.SimpleDateFormat;
// import java.sql.SQLException;
// import java.util.List;
// import java.util.Map;
// import java.util.ArrayList;
// import java.util.StringJoiner;

// public class LoadToClickHouse {
//     private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//     private static final int BATCH_SIZE = 1000; // Configurable batch size for better performance

//     public static void loadData(List<Map<String, List<Object>>> transformedData,
//                               String clickhouseUrl, 
//                               String clickhouseUser, 
//                               String clickhousePassword,
//                               String tableName, 
//                               String databaseName) {
//         if (transformedData == null || transformedData.isEmpty()) {
//             throw new IllegalArgumentException("Transformed data cannot be null or empty");
//         }

//         try (Connection clickhouseConn = DriverManager.getConnection(clickhouseUrl, clickhouseUser, clickhousePassword);
//              Statement statement = clickhouseConn.createStatement()) {
            
//             String primaryKeyColumn = "unique_id";
//             primaryKeyColumn = setupDatabase(statement, databaseName, tableName, transformedData.get(0), primaryKeyColumn);
//             System.out.println("current primary key: " + primaryKeyColumn);
//             insertData(statement, databaseName, tableName, transformedData, primaryKeyColumn);

//         } catch (SQLException e) {
//             throw new RuntimeException("Failed to load data to ClickHouse", e);
//         }
//     }

//     private static String setupDatabase(Statement statement, 
//                                     String databaseName, 
//                                     String tableName,
//                                     Map<String, List<Object>> firstRow,
//                                     String primaryKeyColumn) throws SQLException {
//         statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + databaseName);

//         String createTableSQL = buildCreateTableSQL(databaseName, tableName, firstRow, primaryKeyColumn);
//         statement.executeUpdate(createTableSQL);
//         // statement.executeUpdate("TRUNCATE TABLE " + databaseName + "." + tableName);

//         // We need a primary key for each table to perform INSERT/UPDATE.
//         // Check if the table exists
//         ResultSet rs = statement.executeQuery(
//                 "SELECT COUNT(*) FROM system.tables WHERE database = '" + databaseName + "' AND name = '" + tableName + "';"
//         );
//         boolean tableExists = false;
//         if (rs.next() && rs.getInt(1) > 0) {
//             tableExists = true;
//         }

//         String new_pk = primaryKeyColumn;
//         // If the table doesn't exist, create it with the primary key
//         if (!tableExists) {
//             statement.executeUpdate(createTableSQL);
//             System.out.println("Table created with primary key: " + primaryKeyColumn);
//         } else {
//             System.out.println("Table already exists.");

//             // Check if the table has a primary key
//             ResultSet pkRs = statement.executeQuery(
//                     "SELECT name FROM system.columns WHERE database = '" + databaseName + "' AND table = '" + tableName + "' AND is_in_primary_key = 1;"
//             );
//             boolean hasPrimaryKey = false;
//             while (pkRs.next()) {
//                 if (pkRs.getString(1).equals(primaryKeyColumn)) {
//                     hasPrimaryKey = true;
//                     new_pk =  pkRs.getString(1);
//                 }
//             }

//             // If no primary key, we modify the table to create one
//             if (!hasPrimaryKey) {
//                 statement.executeUpdate(
//                         "ALTER TABLE " + databaseName + "." + tableName + " ADD COLUMN IF NOT EXISTS " + primaryKeyColumn + " UInt32 FIRST, ADD PRIMARY KEY(" + primaryKeyColumn + ");"
//                 );
//                 System.out.println("Primary key added: " + primaryKeyColumn);
//             }
//         }

        
//         System.out.println("Database: [" + databaseName + "], table: [" + tableName + "] checked/created.");
//         return new_pk;
//     }

//     private static String buildCreateTableSQL(String databaseName, 
//                                             String tableName,
//                                             Map<String, List<Object>> firstRow,
//                                             String primaryKeyColumn) {
//         StringJoiner columnDefs = new StringJoiner(", ");
//         // Add the primary key column
//         columnDefs.add(primaryKeyColumn + " UInt64"); // UInt64 for large range of unique IDs

        
//         for (String columnName : firstRow.keySet()) {
//             Object sampleValue = firstRow.get(columnName).get(0);
//             String dataType = getClickHouseType(sampleValue);
//             columnDefs.add(columnName + " " + dataType);
//         }
 

//         return String.format("CREATE TABLE IF NOT EXISTS %s.%s (%s, PRIMARY KEY (%s)) ENGINE = MergeTree() ORDER BY (%s)",
//                                     databaseName, tableName, columnDefs.toString(), primaryKeyColumn, primaryKeyColumn
//             );
//     }

//     private static void insertData(Statement statement, 
//                                  String databaseName,
//                                  String tableName,
//                                  List<Map<String, List<Object>>> transformedData,
//                                  String primaryKeyColumn) throws SQLException {
//         List<String> columnNames = new ArrayList<>(transformedData.get(0).keySet());
//         StringBuilder baseInsertQuery = new StringBuilder()
//             .append("INSERT INTO ")
//             .append(databaseName)
//             .append(".")
//             .append(tableName)
//             .append(" (")
//             .append(String.join(", ", columnNames))
//             .append(") VALUES ");
        

//         int batchCount = 0;
//         StringBuilder batchValues = new StringBuilder();

//         for (Map<String, List<Object>> dataMap : transformedData) {
//             List<Object> firstColumnValues = dataMap.get(columnNames.get(0));
            
//             for (int row = 0; row < firstColumnValues.size(); row++) {
//                 if (batchCount > 0) {
//                     batchValues.append(", ");
//                 }
                
//                 appendRowValues(batchValues, dataMap, columnNames, row);
//                 batchCount++;

//                 if (batchCount >= BATCH_SIZE) {
//                     // executeBatch(statement, baseInsertQuery.toString(), batchValues.toString());
//                     executeUpsertBatchwithPK(statement, baseInsertQuery.toString(), batchValues.toString(), tableName, primaryKeyColumn);
//                     batchValues = new StringBuilder();
//                     batchCount = 0;
//                 }
//             }
//         }

//         // Insert remaining records
//         if (batchCount > 0) {
//             // executeBatch(statement, baseInsertQuery.toString(), batchValues.toString());
//             executeUpsertBatchwithPK(statement, baseInsertQuery.toString(), batchValues.toString(), tableName, primaryKeyColumn);
//         }
//     }

//     private static void executeUpsertBatchwithPK(Statement statement,
//                                                 String baseInsertQuery,
//                                                 String batchValues,
//                                                 String tableName,
//                                                 String primaryKeyColumn) throws SQLException {

//         // Prepare the upsert query (Insert or Update logic)
//         String upsertQuery = baseInsertQuery + batchValues +
//                             " ON DUPLICATE KEY UPDATE " +
//                             primaryKeyColumn + " = VALUES(" + primaryKeyColumn + ")";

//         // Execute the query
//         statement.executeUpdate(upsertQuery);
//         System.out.println("Upsert batch executed successfully.");
//     }

//     private static void appendRowValues(StringBuilder builder,
//                                       Map<String, List<Object>> dataMap,
//                                       List<String> columnNames,
//                                       int row) {
//         StringJoiner values = new StringJoiner(", ", "(", ")");
        
//         for (String columnName : columnNames) {
//             Object value = dataMap.get(columnName).get(row);
//             values.add(formatValue(value));
//         }
        
//         builder.append(values.toString());
//     }

//     private static String formatValue(Object value) {
//         if (value == null) {
//             return "NULL";
//         }
//         if (value instanceof Number) {
//             return "'" + value + "'";
//         }
//         if (value instanceof java.sql.Timestamp || value instanceof java.util.Date) {
//             return "'" + DATE_FORMAT.format(value) + "'";
//         }
//         return "'" + value.toString().replace("'", "\\'") + "'";
//     }

//     // private static void executeBatch(Statement statement, 
//     //                                String baseQuery, 
//     //                                String values) throws SQLException {
//     //     statement.execute(baseQuery + values);
//     // }

//     private static String getClickHouseType(Object value) {
//         if (value == null) return "String";
        
//         if (value instanceof Integer) return "UInt32";
//         if (value instanceof Long) return "UInt64";
//         if (value instanceof String) return "String";
//         if (value instanceof Double || value instanceof Float) return "Float64";
//         if (value instanceof Boolean) return "UInt8";
//         if (value instanceof java.sql.Timestamp 
//             || value instanceof java.util.Date) return "DateTime";
        
//         return "String";
//     }
// }

package org.dbos.apiary.etldemo.etl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
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
                              String databaseName,
                              String primaryKey) {
        if (transformedData == null || transformedData.isEmpty()) {
            throw new IllegalArgumentException("Transformed data cannot be null or empty");
        }

        try (Connection clickhouseConn = DriverManager.getConnection(clickhouseUrl, clickhouseUser, clickhousePassword);
             Statement statement = clickhouseConn.createStatement()) {
            
            setupDatabase(statement, databaseName, tableName, transformedData.get(0), primaryKey);
            insertData(statement, databaseName, tableName, transformedData);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load data to ClickHouse", e);
        }
    }

    private static void setupDatabase(Statement statement, 
                                    String databaseName, 
                                    String tableName,
                                    Map<String, List<Object>> firstRow,
                                    String primaryKey) throws SQLException {
        statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + databaseName);

        String createTableSQL = buildCreateTableSQL(databaseName, tableName, firstRow, primaryKey);
        statement.executeUpdate(createTableSQL);
        // statement.executeUpdate("TRUNCATE TABLE " + databaseName + "." + tableName);
        
        System.out.println("Database: [" + databaseName + "], table: [" + tableName + "] checked/created.");
    }

    private static String buildCreateTableSQL(String databaseName, 
                                            String tableName,
                                            Map<String, List<Object>> firstRow,
                                            String primaryKey) {
        StringJoiner columnDefs = new StringJoiner(", ");
        if (primaryKey == null || primaryKey.isEmpty()) {
            columnDefs.add("synthetic_id UInt32 DEFAULT rowNumberInAllBlocks()");
            primaryKey = "synthetic_id";
        }
        
        for (String columnName : firstRow.keySet()) {
            Object sampleValue = firstRow.get(columnName).get(0);
            String dataType = getClickHouseType(sampleValue);
            columnDefs.add(columnName + " " + dataType);
        }

        return String.format("CREATE TABLE IF NOT EXISTS %s.%s (%s) ENGINE = ReplacingMergeTree(%s) ORDER BY (%s)",
                        databaseName, tableName, columnDefs.toString(), primaryKey, primaryKey);
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

    public static String getPrimaryKey(String postgresUrl, String postgresUser, String postgresPassword, String tableName) throws Exception {
        String primaryKey = null;
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            // Connect to the PostgreSQL database
            connection = DriverManager.getConnection(postgresUrl, postgresUser, postgresPassword);
            statement = connection.createStatement();

            // Query to detect primary keys
            String query = String.format(
                "SELECT a.attname AS column_name, " +
                "CASE WHEN i.indisprimary THEN 'YES' ELSE 'NO' END AS is_primary_key " +
                "FROM pg_attribute a " +
                "JOIN pg_class t ON a.attrelid = t.oid " +
                "LEFT JOIN pg_index i ON t.oid = i.indrelid AND a.attnum = ANY(i.indkey) AND i.indisprimary " +
                "WHERE t.relname = '%s' " +
                "AND a.attisdropped = false " +
                "AND a.attnum > 0;", 
                tableName
            );

                       

            resultSet = statement.executeQuery(query);

            // Retrieve the primary key column name if it exists
            if (resultSet.next()) {
                primaryKey = resultSet.getString("column_name");
            }
            System.out.println("in table" + tableName+ ", pk is: " + primaryKey);
        } finally {
            // Close resources
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        }
        return primaryKey;
    }
}