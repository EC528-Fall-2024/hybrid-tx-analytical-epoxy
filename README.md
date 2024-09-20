# Hybrid Transactional/Analytical Processing using Epoxy

## 0. Team Members
Lukas Chin\
Lucia Gill\
David Li\
Jason Li\
Jiawei Sun

## 1.   Vision and Goals Of The Project:

The goal of this project is to create an implementation of Epoxy to enable accessing multiple databases (MySQL, FoundationDB, DuckDB) or storage systems (S3) through a single interface to allow rapid read and writes between databases.


## 2. User Cases

1. **David**

    - **Wants:** David is the creator of a new social media platform called BUBook and needs to store every piece of information that every user does on his platform including liking posts, followers, favorites, etc.
    - **Needs:** David needs a fast database to collect all of this information, and he wants it to be updated in real-time.

2. **Jason**

    - **Wants:** Jason is the main software developer of BUBook and wants to write a new algorithm every week to get better user retention on the platform.
    - **Needs:** Jason needs to extract, transform, and load all of the data from David's fast database into an analytical database so that he can process the data and write a new and more effective algorithm.

3. **Lucia**

    - **Wants:** Lucia is the marketing manager of BUBook and wants to better advertise the platform so that more new users register and begin using BUBook.
    - **Needs:** Lucia needs to fetch the data from Jason's analytical database to create inferences on making more more captivating digital ads.

** **

## 3.   Scope and Features Of The Project:

The scope of this project is to build a **Hybrid Transactional/Analytical Processing (HTAP)** system using Epoxy, integrating both **OLTP (Online Transaction Processing)** and **OLAP (Online Analytical Processing)** databases through a unified interface. This solution of Epoxy will enable transactions across diverse data stores without relying on the costly two-phase commit protocol.

### In-Scope Features:

1. Integration of OLTP and OLAP Databases

    - Implement an HTAP system using Epoxy to combine OLTP (FoundationDB) and OLAP (DuckDB) databases.
    - Handle both real-time and batched updates with a focus on atomicity (ensuring complete or no transactions) and isolation (transactions are executed independently of each other).

2. OLTP Operations (Handling Transactions)

    - Develop and optimize OLTP operations in FoundationDB for real-time, high-frequency transactions.
    - Support transactional integrity and maintain data consistency within transactions.

3. OLAP Operations (Running Analytics)

    - Implement OLAP operations using DuckDB to handle large-scale analytical queries, enabling batch processing and complex data analysis.
    - Ensure that OLAP queries can efficiently process large datasets without affecting the performance of real-time OLTP transactions.

4. ELT Workflow Development

    - Build an Extract-Transform-Load (ETL) process to transfer data between OLTP and OLAP systems, ensuring seamless updates and synchronization between the two systems.

5. Performance Optimization

    - Optimize the system for performance, including disk page compression and other improvements for minimizing time and resources during transactions.

6. Benchmarking and Testing

    - Benchmark the HTAP system against existing solutions (e.g., Apache Hive) to evaluate its performance on an existing database.

### Out-Of-Scope Features:

1. Two-Phase Commit Protocol

    - The project will not rely on the traditional two-phase commit protocol for ensuring transactional integrity, as Epoxy is designed to operate without it.

2. Modifications to Database Systems

    - There will be no modification on the OLTP and OLAP database systems, since the sole goal of this project is to integrate the two through Epoxy's interface.


** **

## 4. Solution Concept

A high-level architecture of the Hybrid Transactional/Analytical Processing (HTAP) system using Epoxy. The system is designed to handle both transactional and analytical workloads seamlessly, ensuring real-time insights and consistency.

<img width="713" alt="EpoxyArchitecture" src="https://github.com/user-attachments/assets/be94763c-c62c-48c4-a106-143334b6f0c5">

Walkthrough of the Architectural Structure

- Transaction Initiation: A user action (e.g., placing an order, updating a record) triggers the Epoxy layer, which manages the transaction lifecycle and initiates communication with the OLTP database.


- Data Updates: The OLTP database processes the transaction, updating relevant records in real-time. This could involve writing to multiple tables depending on the nature of the operation.


- Replication to OLAP: Epoxy replicates the changes through an ETL workflow to a columnar store (e.g., DuckDB) designed for analytical processing. This replication occurs simultaneously, ensuring that both transactional and analytical databases are updated in real time.


- Conflict Resolution: Before finalizing the transaction, Epoxy performs a consistency check to detect any conflicts between the OLTP and OLAP stores. This step is crucial for maintaining data integrity and ensuring that all changes are valid.


- Commit Process: If no conflicts are detected, the transaction is committed in the OLTP database. At this point, changes become visible across all involved data stores, allowing for immediate data access.


- Real-time Analytics: After the commit, users can run analytical queries on the OLAP store. These queries leverage the most up-to-date transactional data, enabling real-time insights and analytics that can inform decision-making and operational adjustments.

 

Design Implications and Discussion:

The design decisions for the HTAP system focus on leveraging Epoxy's transaction logic to enable fast, batched writes to OLAP data stores without conflicting with the OLTP transaction system. This approach ensures data consistency and integrity across both workloads. Additionally, developing an ETL (Extract-Transform-Load) workflow facilitates efficient data transfer between OLTP and OLAP systems, enhancing real-time analytics capabilities. By prioritizing these elements, the architecture remains robust and adaptable, supporting timely decision-making across various applications while maintaining high performance.

## 5. Acceptance criteria

Minimum Accepted Criteria:

-   Implement Epoxy or build off of another implementation  similar to Epoxy
-   Include a OLTP and OLAP database in the Epoxy layer
-   Write a ELT (Extract-Transform-load) implementation that works with Epoxy and comunicates between the two databases 
-   Test and Benchmark our program with complex databases

Stretch Goals:

-   Implement S3 as database 
-   Improve write performance
-   Work with Massachuset Massachuttes Open Cloud (MOC) 


## 6.  Release Planning:

### Sprints:

-   Find datasets and implement Epoxy
-   Add the two databases onto the Epoxy layer, and add dataset in the OLTP
-   Implement ETL 
-   Benchmark against similar ETL implementations
-   Improve write performance 

* Note: Once our sprint schedule is known, we will update this section with specific sprint/release dates.

## 7.  Resources:

- Epoxy original Paper: https://www.google.com/url?q=https://petereliaskraft.net/res/p2732-kraft.pdf&sa=D&source=editors&ust=1726857165369021&usg=AOvVaw2Pc4xnTr_hCMSMBesATRi4
- Epoxy Simplfied explination: https://www.google.com/url?q=https://muratbuffalo.blogspot.com/2023/11/epoxy-acid-transactions-across-diverse.html&sa=D&source=editors&ust=1726857165369400&usg=AOvVaw1rpSAR_4dFpcT6gvIVj1gX
- Epoxy github: https://github.com/DBOS-project/apiary 

** **

## General comments

Remember that you can always add features at the end of the semester, but you can't go back in time and gain back time you spent on features that you couldn't complete.

** **
