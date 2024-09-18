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

The Scope places a boundary around the solution by detailing the range of features and functions of the project. This section helps to clarify the solution scope and can explicitly state what will not be delivered as well.

It should be specific enough that you can determine that e.g. feature A is in-scope, while feature B is out-of-scope.

** **

## 4. Solution Concept

This section provides a high-level outline of the solution.

Global Architectural Structure Of the Project:

This section provides a high-level architecture or a conceptual diagram showing the scope of the solution. If wireframes or visuals have already been done, this section could also be used to show how the intended solution will look. This section also provides a walkthrough explanation of the architectural structure.

 

Design Implications and Discussion:

This section discusses the implications and reasons of the design decisions made during the global architecture design.

## 5. Acceptance criteria

This section discusses the minimum acceptance criteria at the end of the project and stretch goals.

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

