# OLTP (Postgres)
## Upload existing database to postgres

1. Dentify the PostgreSQL Container ID
    ```shell
    docker ps
    ```

2. Upload the CSV file to your Docker container:
    ```shell
    docker cp /path/to/<your_csv>.csv <id_of_docker_container>:/<your_csv>.csv
    ```

3. Connect to PostgresSQL
    ```shell
    docker exec -it <container_id> psql -U postgres
    ```

4. Create a new database for your dataset
    ```sql
    CREATE DATABASE <your_database_name>;
    ```
    Switch to your dataset: 
    ```sql
    \c <your_database_name>
    ```

5. Create a Table for the CSV Data
    Based on your CSV file, we need to create a table that matches its structure. 
    ```sql
    CREATE TABLE <your_database_name> (
    <column_1> <type_1>,
    <column_2> <type_2> ,
    ...
    );
    ```

6. Import the CSV Data into the Table
    ```sql
    COPY <your_database_name>(<column_1>, <column_2>, ...)
    FROM '/<your_csv>.csv'
    DELIMITER ','
    CSV HEADER;
    ```

6. Check for Existing Tables
    ```sql
    \dt
    ```
    
7. Verify the Data Upload
    ```sql
    SELECT * FROM <your_database_name> LIMIT 10;
    ``` 

# OLAP (ClickHouse)
**Example file: granolaProductDataset.csv**

## Upload existing database to clickhouse
1. Dentify the ClickHouse Container ID
    ```shell
    docker ps
    ```

2. Upload the CSV file to your Docker container:
    ```shell
    docker cp /path/to/<your_csv>.csv <id_of_docker_container>:/<your_csv>.csv
    ```

3. Connect to ClickHouse client
    ```shell
    docker exec -it clickhouse_server clickhouse-client
    ```

4. Create a database for your dataset
    ```sql
    CREATE DATABASE granolaProduct;
    ```
    Switch to your dataset: 
    ```sql
    USE granolaProduct;
    ```

5. Create a Table for the CSV Data
    Based on your CSV file, we need to create a table that matches its structure. 
    ```sql
    CREATE TABLE granolaProduct.granola_products (
        StoreName String,
        BuyDate Date,  -- Use Date type and format as YYYY-MM-DD
        Number UInt32,
        Product String,
        Weight UInt32,
        UnitPrice UInt32,
        City String,
        Month UInt8,
        Year UInt16
    ) ENGINE = MergeTree()
    ORDER BY BuyDate;
    ```

6. Import the CSV Data into the Table
    ```sql
    INSERT INTO granolaProduct.granola_products (StoreName, BuyDate, Number, Product, Weight, UnitPrice, City, Month, Year) 
    FORMAT CSVWithNames
    INFILE '/granolaProductDataset.csv';
    ```

8. Check for Existing Tables
    ```sql
    SHOW TABLES;
    ```

9. Verify the Data Upload
    ```sql
    SELECT * FROM granolaProduct.granola_products LIMIT 10;
    ```
