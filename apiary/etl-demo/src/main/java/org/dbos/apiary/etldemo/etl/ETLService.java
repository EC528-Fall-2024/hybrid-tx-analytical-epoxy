package org.dbos.apiary.etldemo.etl;
import org.springframework.stereotype.Service;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Service
public class ETLService {

    // Function for create timestamp table if not exists for incremental load detection
    public static void createLastExtractedTimeTable(String postgresUrl, String postgresUser, String postgresPassword) {
        Connection connection = null;
        Statement statement = null;

        try {
            // Connect to the PostgreSQL database
            connection = DriverManager.getConnection(postgresUrl, postgresUser, postgresPassword);
            System.out.println("Connected to PostgreSQL!");

            // Define the CREATE TABLE IF NOT EXISTS query
            String createTableQuery = "CREATE TABLE IF NOT EXISTS last_extracted_time (" +
                    "table_name TEXT PRIMARY KEY, " +
                    "last_extracted_time TIMESTAMP" +
                    ")";

            // Execute the query
            statement = connection.createStatement();
            statement.executeUpdate(createTableQuery);

            System.out.println("Table 'last_extracted_time' is ensured to exist.");
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

    // Function for get last extract time for a given table
    public static String getLastExtractedTime(String postgresUrl, String postgresUser, String postgresPassword, String table) {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        String lastExtractedTime = null;

        try {
            connection = DriverManager.getConnection(postgresUrl, postgresUser, postgresPassword);
            statement = connection.createStatement();
            String query = "SELECT last_extracted_time FROM last_extracted_time WHERE table_name = '" + table + "'";
            resultSet = statement.executeQuery(query);

            if (resultSet.next()) {
                lastExtractedTime = resultSet.getString("last_extracted_time");
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
        return lastExtractedTime;
    }

    // Function for update last extract time for a given table
    public static void updateLastExtractedTime(String postgresUrl, String postgresUser, String postgresPassword, String table) {
        Connection connection = null;
        Statement statement = null;
    
        try {
            connection = DriverManager.getConnection(postgresUrl, postgresUser, postgresPassword);
            statement = connection.createStatement();
            // String query = "UPDATE last_extracted_time SET last_extracted_time = current_timestamp WHERE table_name = '" + table + "'";
            String query = "INSERT INTO last_extracted_time (table_name, last_extracted_time) " +
                             "VALUES ('" + table + "', current_timestamp) " +
                             "ON CONFLICT (table_name) DO UPDATE " +
                             "SET last_extracted_time = current_timestamp";
            statement.executeUpdate(query);
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

    public static boolean checkOLAPTableExists(String clickhouseUrl, String clickhouseUser, String clickhousePassword, String databaseName, String tableName) {
        try (Connection connection = DriverManager.getConnection(clickhouseUrl, clickhouseUser, clickhousePassword);
             Statement statement = connection.createStatement()) {
            
            String query = "EXISTS TABLE " + databaseName + "." + tableName;
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                return resultSet.getBoolean(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void createUpdateTrigger(String postgresUrl, String postgresUser, String postgresPassword, String tableName) {
        String triggerName = "set_updated_at"; // Name of the trigger
        String triggerFunction = "CREATE OR REPLACE FUNCTION update_updated_at_column() " +
                                 "RETURNS TRIGGER AS $$ " +
                                 "BEGIN " +
                                 "NEW.updated_at = NOW(); " +
                                 "RETURN NEW; " +
                                 "END; $$ LANGUAGE plpgsql;";
    
        String createTrigger = String.format(
            "CREATE TRIGGER %s BEFORE INSERT OR UPDATE ON %s " +
            "FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();", triggerName, tableName);
    
        String checkTriggerExists = String.format(
            "SELECT 1 FROM pg_trigger WHERE tgname = '%s' AND tgrelid = '%s'::regclass;", 
            triggerName, tableName);
    
            try (Connection connection = DriverManager.getConnection(postgresUrl, postgresUser, postgresPassword);
            Statement statement = connection.createStatement()) {
   
           // Check if the trigger exists
           ResultSet resultSet = statement.executeQuery(checkTriggerExists);
           boolean triggerExists = false;
           if (resultSet.next()) {
               triggerExists = true;
           }
           resultSet.close();
   
           if (triggerExists) {
               System.out.println("Trigger already exists for table: " + tableName + ". Reusing old trigger.");
           } else {
               // Create the trigger function if it doesn't exist
               statement.execute(triggerFunction);
   
               // Create the trigger for the specific table
               statement.execute(createTrigger);
   
               System.out.println("Trigger created successfully for table: " + tableName);
           }
       } catch (SQLException e) {
           System.err.println("Error ensuring trigger for table: " + tableName);
           e.printStackTrace();
       }
    }
    


    public void runETL(String postgresUrl, String postgresUser, String postgresPassword, 
                       String clickhouseUrl, String clickhouseUser, String clickhousePassword) {
        ResultSet resultSet = null;
        Connection postgresConnection = null;
        Statement postgresStatement = null;
        Connection clickhouseConnection = null;
        Statement clickhouseStatement = null;

        System.out.println("---------- 💡 ETL Process Started 💡 ----------");
        try {
            // I. Establish connections and begin transactions
            // This is for make sure we can rollback if ETL failed
            postgresConnection = DriverManager.getConnection(postgresUrl, postgresUser, postgresPassword);
            postgresConnection.setAutoCommit(false); // Start transaction
            postgresStatement = postgresConnection.createStatement();

            clickhouseConnection = DriverManager.getConnection(clickhouseUrl, clickhouseUser, clickhousePassword);
            clickhouseStatement = clickhouseConnection.createStatement();

            // II. Get all table names in current OLTP database
            List<String> tableNames = ExtractFromPostgres.getTableNames(postgresUrl, postgresUser, postgresPassword);

            // Extract database name
            int lastSlashIndex = postgresUrl.lastIndexOf("/");
            String databaseName = postgresUrl.substring(lastSlashIndex + 1);


            // Check and create extract time table if not exists
            createLastExtractedTimeTable(postgresUrl, postgresUser, postgresPassword);

            for (String tableName : tableNames) {
                // Make sure we have trigger for detecting UPDATE and INSERT
                createUpdateTrigger(postgresUrl, postgresUser, postgresPassword, tableName);

                // III. Pre ETL process: Handle deleted rows
                GetDeletedRows.createTrigger(postgresUrl, postgresUser, postgresPassword, tableName);
                List<Map<String, Object>> deletedRows = GetDeletedRows.getDeletedRows(postgresUrl, postgresUser, postgresPassword, tableName);
                if (!deletedRows.isEmpty()) {
                    // When there are rows deleted since last ETL
                    GetDeletedRows.deleteFromOLAP(clickhouseUrl, clickhouseUser, clickhousePassword, tableName, deletedRows);
                    GetDeletedRows.clearProcessedDeletions(postgresUrl, postgresUser, postgresPassword, tableName);
                    System.out.println("Deleted rows synchronized.");
                } else {
                    System.out.println("No deleted rows to process.");
                }

                // IV. Main ETL process
                // Step 1: Extract data from PostgreSQL using provided credentials - incremental load
                // 1.1: Check for a timestamp column
                String timestampColumn = ExtractFromPostgres.checkTimestampColumn(postgresUrl, postgresUser, postgresPassword, tableName);
                String lastExtractedTime = getLastExtractedTime(postgresUrl, postgresUser, postgresPassword, tableName);
                
                // Check if OLAP db has been accidentally deleted
                boolean olapTableExists = checkOLAPTableExists(clickhouseUrl, clickhouseUser, clickhousePassword, databaseName, tableName);
                String loadtype = "";

                
                if (!olapTableExists) {
                    loadtype = "full";
                }

                resultSet = ExtractFromPostgres.extractData(postgresUrl, postgresUser, postgresPassword, tableName, timestampColumn, lastExtractedTime, loadtype);

                
                if (timestampColumn == null) {
                    // If no timestamp column exists, extract everything and add a timestamp column
                    System.out.println("No timestamp column found. Extracting all rows...");
                    
                    // Add a timestamp column for future tracking
                    ExtractFromPostgres.addTimestampColumn(postgresUrl, postgresUser, postgresPassword, tableName);
                } else {
                    // If a timestamp column exists, extract rows based on the timestamp
                    System.out.println("Timestamp column found: " + timestampColumn);

                    if (loadtype == "full") {
                        System.out.println("OLAP database might be accidentally dropped. Performing full load...");
                    }
                }

                // Update last extract time
                updateLastExtractedTime(postgresUrl, postgresUser, postgresPassword, tableName);

                // Get primary key of current table to avoid OLAP duplicate update
                String primaryKey = LoadToClickHouse.getPrimaryKey(postgresUrl, postgresUser, postgresPassword, tableName);

                // Only perform transform and load if we attracted data
                if (resultSet != null && resultSet.isBeforeFirst()) {
                    // Step 2: Transform data from column-based to row-based
                    List<Map<String, List<Object>>> transformedData = TransformService.transformColumnToRow(resultSet);
                    
                    // Step 3: Load the transformed data into ClickHouse in batches
                          
                    // LoadToClickHouse.loadData(transformedData, tableName, clickhouseUrl, clickhouseUser, clickhousePassword);
                    LoadToClickHouse.loadData(transformedData, clickhouseUrl, clickhouseUser, clickhousePassword, tableName, databaseName, primaryKey);
                } else {
                    System.out.println("Already updated since previous ETL!");
                }
                
            }
            // V. Commit transactions
            postgresConnection.commit();
            System.out.println("PostgreSQL transaction committed.");
            System.out.println("---------- 💡 ETL Process Finished 💡 ----------");
            System.out.println("");
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (postgresConnection != null) postgresConnection.rollback();
                System.out.println("PostgreSQL transaction rolled back.");
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
            }
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (postgresStatement != null) postgresStatement.close();
                if (postgresConnection != null) postgresConnection.close();
                if (clickhouseStatement != null) clickhouseStatement.close();
                if (clickhouseConnection != null) clickhouseConnection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
