package org.dbos.apiary.etldemo.etl;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GetDeletedRows {

    // Function for detecting and saving all deleted rows in OLTP table.
    // 1. Table: deleted_rows
    // Structure: table_name | deleted_row_data | deleted_at
    // Purpose: This table stores all the deleted rows from all tables within current OLTP database. We use this for deleted rows ETL process.
    
    // 2. Function: log_deleted_rows()
    // Purpose: Whenever a DELETE query is executed in current OLTP database, this functon logs the deleted data to "deleted_rows" table
    public static void createTrigger(String postgresUrl, String postgresUser, String postgresPassword, String table) {
        Connection connection = null;
        Statement statement = null;

        try {
            connection = DriverManager.getConnection(postgresUrl, postgresUser, postgresPassword);
            statement = connection.createStatement();

            // 1. Create the 'deleted_rows' table if it doesn't exist
            String createDeletedRowsTable = "CREATE TABLE IF NOT EXISTS deleted_rows (" +
                                            "    table_name TEXT NOT NULL, " +
                                            "    deleted_row_data JSONB NOT NULL, " +
                                            "    deleted_at TIMESTAMP DEFAULT now()" +
                                            ");";
            statement.executeUpdate(createDeletedRowsTable);

            // 2. Create the 'log_deleted_rows' function
            String createFunction = "CREATE OR REPLACE FUNCTION log_deleted_rows() " +
                                    "RETURNS TRIGGER AS $$ " +
                                    "BEGIN " +
                                    "    INSERT INTO deleted_rows (table_name, deleted_row_data, deleted_at) " +
                                    "    VALUES (TG_TABLE_NAME, row_to_json(OLD)::JSONB, now()); " +
                                    "    RETURN OLD; " +
                                    "END; " +
                                    "$$ LANGUAGE plpgsql;";

            statement.executeUpdate(createFunction);


            // 3. Create the trigger for the specified table
            String createTrigger = "CREATE TRIGGER capture_deletions " +
                                    "AFTER DELETE ON " + table + " " +
                                    "FOR EACH ROW " +
                                    "EXECUTE FUNCTION log_deleted_rows();";
                                    
            String checkTrigger = "SELECT 1 FROM information_schema.triggers " +
                                    "WHERE event_object_table = '" + table + "' AND trigger_name = 'capture_deletions';";

            ResultSet rs = statement.executeQuery(checkTrigger);
            if (!rs.next()) {
                // Trigger doesn't exist, so create it
                statement.executeUpdate(createTrigger);
            } else {
                System.out.println("DELETE Trigger already exists, reusing it.");
            }

            System.out.println("DELETE Trigger created successfully for table: " + table);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Function to get all deleted rows since last ETL
    public static List<Map<String, Object>> getDeletedRows(String postgresUrl, String postgresUser, String postgresPassword, String table) {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        List<Map<String, Object>> deletedRows = new ArrayList<>();

        try {
            connection = DriverManager.getConnection(postgresUrl, postgresUser, postgresPassword);
            System.out.println("Connected to PostgreSQL!");

            String query = "SELECT deleted_row_data FROM deleted_rows WHERE table_name = '" + table + "'";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String jsonData = resultSet.getString("deleted_row_data");
                ObjectMapper objectMapper = new ObjectMapper(); // Use Jackson or Gson for JSON parsing
                Map<String, Object> rowData = objectMapper.readValue(jsonData, Map.class);
                deletedRows.add(rowData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return deletedRows;
    }

    // Function to delete rows in OLTP database's corresponding table
    public static void deleteFromOLAP(String clickhouseUrl, 
                                    String clickhouseUser, 
                                    String clickhousePassword, 
                                    String table, 
                                    String databaseName,
                                    List<Map<String, Object>> deletedRows) {
        Connection connection = null;
        Statement statement = null;
    
        try {
            connection = DriverManager.getConnection(clickhouseUrl, clickhouseUser, clickhousePassword);
            // System.out.println("Connected to ClickHouse!");
            statement = connection.createStatement();

            for (Map<String, Object> rowData : deletedRows) {
                // Construct the WHERE clause dynamically
                StringBuilder whereClause = new StringBuilder();
                for (Map.Entry<String, Object> entry : rowData.entrySet()) {
                    // Skip the "updated_at" column
                    if ("updated_at".equals(entry.getKey())) {
                        continue;
                    }

                    if (whereClause.length() > 0) {
                        whereClause.append(" AND ");
                    }
                    if (entry.getValue() instanceof String) {
                        whereClause.append(entry.getKey())
                                .append(" = '")
                                .append(entry.getValue().toString().replace("'", "\\'"))
                                .append("'");
                    } else {
                        whereClause.append(entry.getKey())
                                .append(" = ")
                                .append(entry.getValue());
                    }
                }
    
                // Construct the DELETE query
                String deleteQuery = String.format(
                    "ALTER TABLE %s.%s DELETE WHERE %s",
                    databaseName, table, whereClause.toString()
                );
                
                // Execute the DELETE query
                System.out.println("Executing: " + deleteQuery);
                statement.execute(deleteQuery);
            }
            System.out.println("Deleted rows from ClickHouse table.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // After syncing deletions to OLAP, remove the corresponding entries from the deleted_rows table.
    public static void clearProcessedDeletions(String postgresUrl, String postgresUser, String postgresPassword, String table) {
        Connection connection = null;
        Statement statement = null;
    
        try {
            connection = DriverManager.getConnection(postgresUrl, postgresUser, postgresPassword);
            System.out.println("Connected to PostgreSQL!");
    
            String query = "DELETE FROM deleted_rows WHERE table_name = '" + table + "'";
            statement = connection.createStatement();
            statement.executeUpdate(query);
    
            System.out.println("Cleared processed deletions for table: " + table);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}