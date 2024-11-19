# ETL Tutorial and Demo for ETL website

This tutorial will show you how to use our ETL website to perform ETL on public data. 

**Only follow this if you want to perform the ETL on public data. For local data or private follow the README.md insdie the apiary folder**


### Website Url

https://etl-service-hybrid-tx-analytical-epoxy-31f481.apps.shift.nerc.mghpcc.org/ 



### Inputs
#### OLTP DATABASES
  URL: jdbc:<>://<PUBLIC IP>:<DATABASE PUBLIC PORT>/<DATABASE NAME>
  USERNAME: user name of database
  PASSWORD: pasword to access database
  
### OLAP DATABASES
  URL: jdbc:<>://<PUBLIC IP>:<DATABASE PUBLIC PORT>/<DATABASE NAME>
  USERNAME: user name of database
  PASSWORD: pasword to access database

### Examples of URLs

Postgres: jdbc:postgresql://<PUBLIC IP>:5432/<DATABASE NAME>

MySQL: jdbc:mysql://<PUBLIC_IP>:3306/<DB_NAME>

ClickHouse: jdbc:clickhouse://<PUBLIC_IP>:8123/<DB_NAME>


### Start ETL Process
Once the urls have been types into the appropriate boxes, the click on start ETL. There should be text indicating the process has started, and once it completes it should show these metrics:

Execution Time, Memory Usage, Network Info, Downlink, Latency, and CPU Info
  
*If the databases were not accessed the process will still run, check inside the database to make sure that the process was completed.*

