# ETL Tutorial and Demo for ETL website

This tutorial will show you how to use our ETL website to perform ETL on public data. 

**Only follow this if you want to perform the ETL on public data. For local data or private follow the README.md insdie the apiary folder**


### Website Url

https://etl-service-hybrid-tx-analytical-epoxy-31f481.apps.shift.nerc.mghpcc.org/ 

---

### Inputs
#### OLTP DATABASES
 # Database Connection Information


- **URL**: `jdbc:<NAME OF OLTP DATABASE>://<PUBLIC IP>:<DATABASE PUBLIC PORT>/<DATABASE NAME>`
- **USERNAME**: *username of the database*
- **PASSWORD**: *password to access the database*

## OLAP Database Connection
- **URL**: `jdbc:<NAME OF OLAP DATABASE>://<PUBLIC IP>:<DATABASE PUBLIC PORT>/<DATABASE NAME>`
- **USERNAME**: *username of the database*
- **PASSWORD**: *password to access the database*

---

## Examples of Connection URLs

Here are examples of how to structure JDBC URLs for specific database types:

- **PostgreSQL**:  
  `jdbc:postgresql://<PUBLIC IP>:5432/<DATABASE NAME>`

- **MySQL**:  
  `jdbc:mysql://<PUBLIC IP>:3306/<DATABASE NAME>`

- **ClickHouse**:  
  `jdbc:clickhouse://<PUBLIC IP>:8123/<DATABASE NAME>`

Replace each placeholder with your specific database information to ensure successful connections.

---

### Start ETL Process
Once the urls have been types into the appropriate boxes, the click on start ETL. There should be text indicating the process has started, and once it completes it should show these metrics:

Execution Time, Memory Usage, Network Info, Downlink, Latency, and CPU Info
  
*If the databases were not accessed the process will still run, check inside the database to make sure that the process was completed.*

