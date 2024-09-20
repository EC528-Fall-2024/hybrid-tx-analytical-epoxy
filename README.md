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

This section provides a high-level outline of the solution.

Global Architectural Structure Of the Project:

This section provides a high-level architecture or a conceptual diagram showing the scope of the solution. If wireframes or visuals have already been done, this section could also be used to show how the intended solution will look. This section also provides a walkthrough explanation of the architectural structure.

 

Design Implications and Discussion:

This section discusses the implications and reasons of the design decisions made during the global architecture design.

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

Release planning section describes how the project will deliver incremental sets of features and functions in a series of releases to completion. Identification of user stories associated with iterations that will ease/guide sprint planning sessions is encouraged. Higher level details for the first iteration is expected.

** **

## General comments

Remember that you can always add features at the end of the semester, but you can't go back in time and gain back time you spent on features that you couldn't complete.

** **

For more help on markdown, see
https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet

In particular, you can add images like this (clone the repository to see details):

![alt text](https://github.com/BU-NU-CLOUD-SP18/sample-project/raw/master/cloud.png "Hover text")

