# Apiary ETL Tutorial and Demo

This tutorial will show you how to build a simple social network
web application using Apiary and [Spring Boot](https://spring.io/projects/spring-boot) for ETL process.
We use Apiary's Postgres (for OLTP) and ClickHouse (for OLAP) as backends.

**Only follow this if you want to perform the ETL on private local data. For data with public IPs please look at our CloudReadMe.md**




To get started, let's first install some dependencies:

```shell
sudo apt install openjdk-11-jdk maven libatomic1
```

Next, let's compile Apiary. In the Apiary root directory, run:

```shell
mvn -DskipTests package
```


Then, let's start Docker images for Postgres and ClickHouse. We recommend you [configure Docker](https://docs.docker.com/engine/install/linux-postinstall/) so it can be run by non-root users.

- For Postgres
    ```
    scripts/initialize_postgres_docker.sh
    ```

- For ClickHouse
    ```shell
    scripts/initialize_clickhouse_docker.sh
    ```


Everything's ready!  To start the site, run in the `etl-demo` root directory:
```shell
mvn clean && mvn package && mvn spring-boot:run
```

Then, navigate to `localhost:8081` to view this new social network! You should see the Nectar homepage:
<img src='app_screenshot.jpg' width="500">

We can start ETL process by clicking on the button.

### Example parameters

- OLTP Database URL: `jdbc:postgresql://localhost:5432/campaign_product_subcategory`
- OLTP Username: `postgres`
- OLTP Password: `dbos`
- OLAP Database URL: `jdbc:clickhouse://localhost:8123`
- OLAP Username: `default`
- OLAP Password: (left empty)


## Function Description
We want to build a simple application where you can do ETL process from Postgres Database to ClickHouse, i.e. OLTP to OLAP.

### Data Preperation 
The first thing we need to do is import a dataset in Postgres
to store the information our site needs. Instructions for adding dataset to OLTP and OLAP is described in this [UPDATE_DS.md](https://github.com/EC528-Fall-2024/hybrid-tx-analytical-epoxy/blob/main/apiary/UPLOAD_DS.md). Our goal is to perform ETL on a simple `campaign_product_subcategory` dataset.

Note here, we DO NOT need to import any dataset to ClickHouse since we will detect and create new datasets depends on the given OLTP dataset in Postgres.


### ETL Flow: Extract, Transform, Load

The ETL process will perform a **incremental load** for data from Postgre to ClickHouse using the following classes:

- `ETLService.java`: Preprocess and orchestrates the ETL process and ensures the data is moved smoothly.
- `ExtractFromPostgres.java`: Extracts the data from PostgreSQL into a ResultSet.
- `TransformService` : Transforms the data from column based to row base.
- `LoadToClickHouse.java`: Loads the extracted data from PostgreSQL into ClickHouse.

Let's take a closer look at these functions.


### `Extraction`
Before considering the logic behind it, we need to establish connection with Postgres. If you constructed docker container differently, then you might need to modify these as your need.
```java
private static final String POSTGRES_URL = "jdbc:postgresql://localhost:5432/campaign_product_subcategory";
private static final String POSTGRES_USER = "postgres";
private static final String POSTGRES_PASSWORD = "dbos";

// Establish Connection
Connection connection = DriverManager.getConnection(POSTGRES_URL, POSTGRES_USER, POSTGRES_PASSWORD);
System.out.println("Connected to PostgreSQL!");
```
Next, to implement incremental load, we need to perform comparison between current table and table since previous ETL. Instead of using log file detection, which might not be initialized by the database setting, we choose to alter tables in OLTP database with timestamp for comparison.

Incremental load means that we want to only load what has been modified since previous ETL. Modification can be categorized into three types: `INSERT`, `UPDATE`, and `DELETE`. Let's analyze this by cases:

#### 1. `INSERT` (in `ExtractFromPostgres.java`)
To keep track of incremented rows, we want to perform time comparison. That is to say, we need to perform comparison between
$$\textbf{Previous extrction time } \text{and} \textbf{ Row update time}$$

To achieve this, we need to guarentee the following things:

- Each table has its corresponding last extracted time.
    
- There is a timestamp that saves update time for each row in each table of current OLTP database.

We implemented the function with the following structure.

**a. Create table `last_extracted_time`:**
| table_name    | last_extracted_time           |
| --------      | -------                       |
| A             | 2024-11-09 20:10:34.273651    |
| B             | 2024-11-09 20:18:12.165294    |

`table_name` : name of the table in current OLTP database

`last_extracted_time`: time that we performed ETL on this table

```java
String createTableQuery = "CREATE TABLE IF NOT EXISTS last_extracted_time (" +
                        "table_name TEXT PRIMARY KEY, " +
                        "last_extracted_time TIMESTAMP" +
                        ")";

// Execute the query
Statement statement = connection.createStatement();
statement.executeUpdate(createTableQuery);
```

**b. Create (if doesn't exist) timestamp column `updated_at` for each table:**
| name      | age       | updated_at                    |
| --------  | -------   | ------                        |
| Jiawei    | 25        | 2024-11-09 20:10:34.273651    |
| Lucia     | 21        | 2024-11-09 20:18:12.165295    |
| David     | 19        | 2024-11-09 20:18:12.165294    |

```java
String alterTableQuery = "ALTER TABLE " + table + " ADD COLUMN updated_at TIMESTAMP DEFAULT current_timestamp";
Statement statement = connection.createStatement();
statement.executeUpdate(alterTableQuery);
```

**c. Extract correct rows**

First, we extract `last_extracted_time` and `updated_at` 
After preperation, we can start ETL by time comparison. If for a row in a table, 
<p style="text-align:center;">last_extracted_time < updated_at</p>
then this row has been updated after last ETL. Thus, we need to perform extract on this row.

For the first time we perform ETL, if there's no table `last_extracted_time` or no `updated_at` column for each table, we perform full extraction. Else, we compare the time and  extract only updated rows.

```java
Statement statement = connection.createStatement();
String query;
if (timestampColumn == null || lastExtractedTime == null) {
    query = "SELECT * FROM " + table;
} else {
    query = "SELECT * FROM " + table + " WHERE " + timestampColumn + " > '" + lastExtractedTime + "'";
}
resultSet = statement.executeQuery(query);
```

**d. Update `last_extracted_time` table:**

After extraction, we will update `last_extracted_time`, set current time as the latest extraction time for corrsponding table for next ETL comparison use.




#### 2. `DELETE` (in `ETLService.java`)
Sometimes, when we "delete" a message in social media, we are not actually removing data from dataset. Instead, its marked as "deleted" and not displayed. This way of "deletion" is considered as `UPDATE` instead of `DELETE`. What we truely discuss here is removing a row from a table with `DELETE` event occurs.

If a row is removed, we cannot synchronize it by comparing update time. Thus, we need to have another temporary table storing all data we deleted since last ETL. That is to say, everytime `DELETE` event happens, we need to auto detect and record what has been deleted. We can implement it by creating trigger function with `DELETE` as condition.

**a. Create table `deleted_rows`:**
| table_name    | deleted_row_data              | deleted_at                 |
| --------      | -------                       | ------                     |
| A             | {"name": Jiawei, "age": 25}   | 2024-11-09 20:10:34.273651 |
| A             | {"name": Lucia, "age": 21}    | 2024-11-09 20:09:28.369471 |

This table stores deleted rows' content in `JSONB` format, which is the binary format of JSON string, for faster for querying, indexing, and updates. We also have the correcponding `table_name` for cleaning at last.

```java
String createDeletedRowsTable = "CREATE TABLE IF NOT EXISTS deleted_rows (" +
                                "    table_name TEXT NOT NULL, " +
                                "    deleted_row_data JSONB NOT NULL, " +
                                "    deleted_at TIMESTAMP DEFAULT now()" +
                                ");";
statement.executeUpdate(createDeletedRowsTable);
```

**b. Create function `log_deleted_rows`:**

This function is a trigger function that defines what should happen when the trigger is fired. Here, we define the function to log rows deleted from a table into `deleted_rows`.

```java
String createFunction = "CREATE OR REPLACE FUNCTION log_deleted_rows() " +
                        "RETURNS TRIGGER AS $$ " +
                        "BEGIN " +
                        "    INSERT INTO deleted_rows (table_name, deleted_row_data, deleted_at) " +
                        "    VALUES (TG_TABLE_NAME, row_to_json(OLD)::JSONB, now()); " +
                        "    RETURN OLD; " +
                        "END; " +
                        "$$ LANGUAGE plpgsql;";

statement.executeUpdate(createFunction);
```

**c. Create trigger `capture_deletions`:**

A trigger is an *event listener* that is attached to a specific table. The trigger listens for specific events (here, only `DELETE`) and fires a predefined function (here, `log_deleted_rows`) when the event occurs.

Postgres doesn't support `CREATE OR REPLACE` for triggers since they are closely tied to the table they are applied to and can have complex dependencies. Allowing `CREATE OR REPLACE` might inadvertently cause data or logic inconsistencies. Hence, PostgreSQL enforces dropping and recreating the trigger manually.

With is requirement, since we are using same logic to trigger, and reusing the same trigger is faster than creating a new trigger for frequent ETL, we will only create trigger if it doesn't exists.

```java
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
    System.out.println("Trigger already exists, reusing it.");
}
```

**d. Perform ETL on `deleted_row_data`:**

After preparing the table and trigger, we can perform ETL on deleted data. 

First, we query the `deleted_row_data` table to get all deleted rows.
```java
String query = "SELECT deleted_row_data FROM deleted_rows WHERE table_name = '" + table + "'";
Statement statement = connection.createStatement();
ResultSet resultSet = statement.executeQuery(query);

while (resultSet.next()) {
    String jsonData = resultSet.getString("deleted_row_data");
    ObjectMapper objectMapper = new ObjectMapper(); // Use Jackson or Gson for JSON parsing
    Map<String, Object> rowData = objectMapper.readValue(jsonData, Map.class);
    deletedRows.add(rowData);
}
```

Then, we transform the row based data to column based format for deletion in OLAP.
```java
// Convert row data to column data. i.e. we perform transform to the deleted rows
List<String> columnsToDelete = deletedRows.stream()
                                            .map(row -> (String) row.get("table_name"))
                                            .filter(columnName -> columnName != null) // Filter out any nulls
                                            .collect(Collectors.toList());
```

Last, we delete data in OLAP by dropping the corresponding columns.
```java
for (String column : columnsToDelete) {
    String query = "ALTER TABLE " + table + " DROP COLUMN IF EXISTS `" + column + "`";
    statement = connection.createStatement();
    statement.executeUpdate(query);
    System.out.println("Deleted column from OLAP: " + column);
}
```

**e. Clear `deleted_rows` after ETL:**

After syncing deletions to OLAP, remove the corresponding entries from the `deleted_rows` table.
```java
String query = "DELETE FROM deleted_rows WHERE table_name = '" + table + "'";
Statement statement = connection.createStatement();
statement.executeUpdate(query);
```

### `Load`
**a. Establishing Connection:**

ClickHouse has two default ports: 8123 for HTTP interface and 9000 for the native TCP/IP protocol. We use 8123 as the JDBC driver defaults to HTTP-based communication.

```java
private static final String CLICKHOUSE_URL = "jdbc:clickhouse://localhost:8123";
private static final String CLICKHOUSE_USER = "default";
private static final String CLICKHOUSE_PASSWORD = "";

Connection clickhouseConn = DriverManager.getConnection(CLICKHOUSE_URL, CLICKHOUSE_USER, CLICKHOUSE_PASSWORD);
System.out.println("Connected to ClickHouse!");
```

**b. Connection Detection**

To ensure the connection is valid, include a check for clickhouseConn:

```java
if (clickhouseConn != null) {
    statement = clickhouseConn.createStatement();
} else {
    System.err.println("ClickHouse connection is null!");
}
```
Here's the reason why: 

ClickHouse JDBC drivers generally prefer the **HTTP protocol** because it allows them to leverage the full power of REST APIs, and it’s easier to work with across firewalls and various systems.

Since our JDBC connection string begins with `jdbc:clickhouse://` and it is HTTP-based, it defaults to 8123.

Port 9000 is only used when we configure the connection to use a native driver or binary protocol.

**c. Database and Table Detection:**

Before loading data, ensure the target database and table exist. If they don't, create them dynamically.

- Create Database if doesn't exist
    ```java
    Statement statement = statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + databaseName);
    ```

- Dynamically generate table schemas based on the data's structure.
    ```java
    StringBuilder createTableSQL = new StringBuilder("CREATE TABLE IF NOT EXISTS " 
                                                    + databaseName + "." 
                                                    + tableName 
                                                    + " (\n" + "feature_name String");

    // Add columns based on the first value of each feature
    for (Map.Entry<String, List<Object>> entry : firstRow.entrySet()) {
        List<Object> values = entry.getValue();
        if (!values.isEmpty()) {
            Object firstValue = values.get(0);
            String columnType = getClickHouseType(firstValue);
            createTableSQL.append(",\n").append(entry.getKey())
                        .append(" ").append(columnType);
        }
    }
    
    createTableSQL.append(") ENGINE = MergeTree() ORDER BY feature_name");
    statement.executeUpdate(createTableSQL.toString());
    statement.executeUpdate("TRUNCATE TABLE " + databaseName + "." + tableName);
    ```

- Insert data
    ```java
    for (Map<String, List<Object>> row : transformedData) {
        for (Map.Entry<String, List<Object>> entry : row.entrySet()) {
            String columnName = entry.getKey();
            List<Object> values = entry.getValue();

            // Format datetime values properly
            StringBuilder valuesStr = new StringBuilder();
            valuesStr.append("('").append(columnName).append("'");
            
            for (Object value : values) {
                if (value instanceof java.sql.Timestamp || value instanceof java.util.Date) {
                    // Format datetime as 'YYYY-MM-DD HH:MM:SS'
                    String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(value);
                    valuesStr.append(",'").append(formattedDate).append("'");
                } else {
                    valuesStr.append(",'").append(value).append("'");
                }
            }
            valuesStr.append(")");

            String insertSQL = "INSERT INTO " + databaseName + "." + tableName + 
                            " VALUES " + valuesStr.toString();
            
            statement.executeUpdate(insertSQL);
        }
    }
    ```

## Tying it Together
With our functions written, it's almost time to launch our site.
We'll now tell the [Spring controller](src\main\java\com\clickhousedemo\Application.java)
to launch an Apiary worker on startup to start the application:
```java
@SpringBootApplication
public class ETLDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(ETLDemoApplication.class, args);
    }
}
```
