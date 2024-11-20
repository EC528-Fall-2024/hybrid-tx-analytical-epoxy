# Hybrid Transactional/Analytical Processing using Epoxy

## Team Members

| Name       | Email           |
| ---------- | --------------- |
| Lukas Chin | lchin10@bu.edu  |
| Lucia Gill | luciagil@bu.edu |
| David Li   | dav@bu.edu      |
| Jason Li   | jli3469@bu.edu  |
| Jiawei Sun | alinajw@bu.edu  |

## Mentors

| Name           | Email              |
| -------------- | ------------------ |
| Dan Lambridght | dlambrig@gmail.com |
| Orran Krieger  | okrieg@bu.edu      |
| Ata Turk       | ataturk@bu.edu     |

## 0. Introduction to Epoxy

1. **What is Epoxy**

   Epoxy is a protocol for providing ACID transactions across diverse data stores. It uses a primary transactional DBMS as a transaction coordinator for transactions among it and several potentially non-transactional secondary data stores. Epoxy is implemented in shim layers co-located with these data stores which intercept and interpose on client requests, but do not require modifications to the stores themselves.
2. **Basic Structure**

   Epoxy leverages Postgres transactional database as the coordinator and  extends multiversion concurrency control (MVCC) for cross-data store isolation. It provides isolation as well as atomicity and durability through its optimistic concurrency control (OCC) plus two-phase commit (2PC) protocol. Epoxy was implemented as a bolt-on shim layer for five diverse data stores: Postgres, MySQL, Elasticsearch, MongoDB, and Google Cloud Storage (GCS).
3. **How it functions**

   - Each Epoxy transaction is linked to a snapshot, representing the set of all past transactions visible to it.
   - Epoxy secondary store shims enhance record versions with metadata to facilitate transactional read operations. Visibility of a record version to a transaction is determined by the presence of beginTxn in the transaction's snapshot, and the absence of endTxn in the transaction's snapshot.
   - Due to the two-phase commit (2PC) protocol used by Epoxy, the secondary stores prepare first inside their databases, and then the primary concludes the transaction commit (or the abort) if all operations succeed (or any operation fail) on all data stores.

## 1.   Vision and Goals Of The Project:

The goal of this project is to create an implementation of Epoxy to enable accessing multiple databases (MySQL, ClickHouse, Postgres DB) or storage systems (S3) through a single interface to allow rapid read and writes between databases.

## 2. User Cases

1. **David**

   - **Wants:** David is the creator of a new social media platform called BUBook and needs to store every piece of information that every user does on his platform including liking posts, followers, favorites, etc.
   - **Needs:** David needs a fast database to collect all of this information, and he wants it to be updated in real-time.
   - **How:** When David likes a post, a write operation is triggered. Epoxy (coordinator) then starts the transaction, handling the necessary write operations to an **OLTP** database, which might be retrieving a row with all columns, then update specific columns. Next, the transaction is committed if all operations are successful in the related data stores.

<br>

2. **Jason**

   - **Wants:** Jason is the main software developer of BUBook and wants to write a new algorithm every week to get better user retention on the platform.
   - **Needs:** Jason needs to extract, transform, and load all of the data from David's fast database into an analytical database so that he can process the data and write a new and more effective algorithm.
   - **How:** Jason uses an ETL (Extract, Transform, Load) pipeline to process the data. First, he extracts the relevant data from David's OLTP database. Then, he transforms this data into a format suitable for analysis, potentially aggregating user actions, calculating engagement metrics, and deriving insights. Finally, he loads this processed data into an OLAP database optimized for complex queries and data analysis. This ETL process runs periodically, ensuring Jason has up-to-date data for developing and refining his user retention algorithms.

<br>

3. **Lucia**

   - **Wants:** Lucia is the marketing manager of BUBook and wants to better advertise the platform so that more new users register and begin using BUBook.
   - **Needs:** Lucia needs to fetch the data from Jason's analytical database to create inferences on making more captivating digital ads.
   - **How:** When Lucia fetches the data, a read operation is triggered. Epoxy then starts the transaction, handling the necessary write operations to an **OLAP** database, which might be retrieving informations from a column with all the rows. Next, the transaction is committed if all operations are successful in the related data stores.

---

## 3.   Scope and Features Of The Project:

The scope of this project is to build a **Hybrid Transactional/Analytical Processing (HTAP)** system using Epoxy, integrating both **OLTP (Online Transaction Processing)** and **OLAP (Online Analytical Processing)** databases through a unified interface. This solution of Epoxy will enable transactions across diverse data stores without relying on the costly two-phase commit protocol. We will provide a cloud and local version.

<br>

### In-Scope Features:

1. Integration of OLTP and OLAP Databases

   - Implement an HTAP system using Epoxy to combine OLTP (Postgress) and OLAP (ClickHouse) databases.
   - Handle both real-time and batched updates with a focus on atomicity (ensuring complete or no transactions) and isolation (transactions are executed independently of each other).
2. OLTP Operations (Handling Transactions)

   - Develop and optimize OLTP operations in mySQL  for real-time, high-frequency transactions.
   - Support transactional integrity and maintain data consistency within transactions.
3. OLAP Operations (Running Analytics)

   - Implement OLAP operations using DuckDB to handle large-scale analytical queries, enabling batch processing and complex data analysis.
   - Ensure that OLAP queries can efficiently process large datasets without affecting the performance of real-time OLTP transactions.
4. ETL Workflow Development (new feature different from vanilla epoxy)

   - Build an Extract-Transform-Load (ETL) process to transfer data between OLTP and OLAP systems, ensuring seamless updates and synchronization between the two systems.
   - Improve write efficiency in ETL data transfer process with same level of isolation with Epoxy (ideally).
5. Performance Optimization

   - Optimize the system for performance, including disk page compression and other improvements for minimizing time and resources during transactions.
6. Benchmarking and Testing

   - Benchmark the HTAP system against existing solutions (e.g., Apache Hive) to evaluate its performance on an existing database.

<br>

### Out-Of-Scope Features:

1. **Two-Phase Commit Protocol**

   - Definition: two-phase commit protocol (2PC, tupac) is a type of atomic commitment protocol (ACP). It is a distributed algorithm that coordinates all the processes that participate in a distributed atomic transaction on whether to commit or abort (roll back) the transaction.
   - Two-phases:

     In *commit-request phase* (or voting phase), the coordinator process sends a prepare request to all the participating nodes. Each participant tries to execute the transaction locally. If the local transaction was successful and the participant is ready to commit, it vote "Yes". Else, it vote "No".

     In the *commit phase*, based on voting of the participants, the coordinator decides whether to commit (only if **all** have voted "Yes") or abort the transaction (if **any** has voted "No"), and notifies the result to all the participants. The participants then follow with the needed actions (commit or abort) with their local transactional resources (also called recoverable resources; e.g., database data) and their respective portions in the transaction's other output (if applicable).
   - Related resources: [Wikipedia 2PC Definition](https://en.wikipedia.org/wiki/Two-phase_commit_protocol),
     [Two-phase commit and beyond](https://muratbuffalo.blogspot.com/2018/12/2-phase-commit-and-beyond.html) by [MURAT](https://www.blogger.com/profile/07842046940394980130)
   - The project will not rely on the traditional two-phase commit protocol for ensuring transactional integrity, as Epoxy is designed to operate without it.
2. **Modifications to Database Systems**

   - There will be no modification on the OLTP and OLAP database systems, since the sole goal of this project is to integrate the two through Epoxy's interface.

---

## 4. Solution Concept

A high-level architecture of the Hybrid Transactional/Analytical Processing (HTAP) system using Epoxy. The system is designed to handle both transactional and analytical workloads seamlessly, ensuring real-time insights and consistency.

<p align="center">
<img src="https://github.com/user-attachments/assets/e37eb3e6-9b3b-4c2b-b276-7afd557e88fc" width="80%">
</p>
<p align="center">
Architectural Diagram from Paper
</p>

<p align="center">
<img src="https://github.com/user-attachments/assets/fd16707c-8400-4eb8-9e4f-00b2ccd5aead" width="80%">
</p>
<p align="center">
Simplified View of HTAP Diagram
</p>

Walkthrough of the Architectural Structure:

- **Client Requests**: User actions trigger requests that are received by the Hybrid Transactional/Analytical Processing Database system.
- **Transaction Coordinator**: A user action (e.g., placing an order, updating a record) triggers the Epoxy layer, which manages the transaction lifecycle and initiates communication with the OLTP database.
- **Transactional Database**: An Online Transaction Processing (OLTP) database (e.g. mySQL, FoundationDB) processes and stores transactional data in real-time.
- **Epoxy Shim**: Executes local operations and interacts with the Transaction Coordinator to ensure
- **Data Updates**: The OLTP database processes the transaction, updating relevant records in real-time. This could involve writing to multiple tables depending on the nature of the operation.
- **ETL Pipeline**: Epoxy replicates the changes through an ETL workflow to a columnar store (e.g., DuckDB) designed for analytical processing. This replication occurs simultaneously, ensuring that both transactional and analytical databases are updated in real time.
- **Conflict Resolution**: Before finalizing the transaction, Epoxy performs a consistency check to detect any conflicts between the OLTP and OLAP stores. This step is crucial for maintaining data integrity and ensuring that all changes are valid.
- **Commit Process**: If no conflicts are detected, the transaction is committed in the OLTP database. At this point, changes become visible across all involved data stores, allowing for immediate data access.
- **Real-time Analytics**: After the commit, users can run analytical queries on the OLAP store. These queries leverage the most up-to-date transactional data, enabling real-time insights and analytics that can inform decision-making and operational adjustments.

Design Implications and Discussion:

The design decisions for the HTAP system focus on leveraging Epoxy's transaction logic to enable fast, batched writes to OLAP data stores without conflicting with the OLTP transaction system. This approach ensures data consistency and integrity across both workloads. Additionally, developing an ETL (Extract-Transform-Load) workflow facilitates efficient data transfer between OLTP and OLAP systems, enhancing real-time analytics capabilities. By prioritizing these elements, the architecture remains robust and adaptable, supporting timely decision-making across various applications while maintaining high performance.

## 5. Acceptance Criteria

Minimum Viable Product:

- Implement the distributed transaction coordination system or develop a similar middleware for managing database operations across multiple data stores
- Include a OLTP and OLAP database in the Epoxy layer
- Write a ETL (Extract-Transform-load) implementation that works with Epoxy and comunicates between the two databases
- Test and Benchmark our program with complex databases

Stretch Goals:

- Implement S3 as database
- Improve write performance
- Work with Massachuset Massachuttes Open Cloud (MOC)

## 6.   How to utilize this Github:

first identify if your databses are local or if they are public. Both databases must be either both local or both public.

If the databases are **public**:
- Make sure both databases have public IP, a public port number, and that you know the username and pasword
- Access the website throught this link : https://etl-service-hybrid-tx-analytical-epoxy-31f481.apps.shift.nerc.mghpcc.org/
- For more explination or step by step explination follow the steps inside the CloudReadMe.md file inside the ETL folder.
      
If the databases are **local**:
- Clone this GitHub
- Save the docker image inside Apiary scripts folder 
- Follow the steps inside the README.md file insside the ETL folder.


## 7.  Release Planning:

### Sprints:

Sprint 1. Find datasets and implement Epoxy <br>
[Sprint video](https://youtu.be/DFNJatZPwU4) | [Sprint slides](https://docs.google.com/presentation/d/17updjwpJJet0NcEK1xSxx1LHyAb83xCzIRcUtQwE-CQ/edit?usp=sharing)

Sprint 2. Add the two databases onto the Epoxy layer, and add dataset in the OLTP <br>
[Sprint video](https://www.youtube.com/watch?v=xwD-pXaUsdU) | [Sprint slides](https://docs.google.com/presentation/d/19ie7AyahpwhMFPn3A1KhOjO3vDkOWZsVh58izebvY8w/edit?usp=sharing)

Sprint 3. Implement ETL <br>
[Sprint video](https://www.youtube.com/watch?v=i8_xEcuMeO4) | [Sprint slides](https://docs.google.com/presentation/d/1JaS7F-uORPoyf4aQPv05HhkPrSkrDUldQeUTgYz-IpQ/edit?usp=sharing)

Sprint 4. Benchmark against similar ETL implementations <br>
[Sprint video](https://youtu.be/FSBOGl79jeM) | [Sprint slides](https://docs.google.com/presentation/d/1KjUBKjwwSP4n8LdfrPmf04ilOiCuZbRyyk79-6OXjyQ/edit?usp=sharing)

Sprint 5. Improve write performance

 Note: Once our sprint schedule is known, we will update this section with specific sprint/release dates.

## 8.  Resources:

- Epoxy original Paper: https://www.google.com/url?q=https://petereliaskraft.net/res/p2732-kraft.pdf&sa=D&source=editors&ust=1726857165369021&usg=AOvVaw2Pc4xnTr_hCMSMBesATRi4
- Epoxy Simplfied explination: https://www.google.com/url?q=https://muratbuffalo.blogspot.com/2023/11/epoxy-acid-transactions-across-diverse.html&sa=D&source=editors&ust=1726857165369400&usg=AOvVaw1rpSAR_4dFpcT6gvIVj1gX
- Epoxy github: https://github.com/DBOS-project/apiary
- ETL Cloud Website: https://etl-service-hybrid-tx-analytical-epoxy-31f481.apps.shift.nerc.mghpcc.org/

---
