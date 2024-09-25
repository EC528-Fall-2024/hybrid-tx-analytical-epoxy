
# New Apiary Tutorial and Demo

This tutorial will show you how to build a simple social network web application using Apiary and [Spring Boot](https://spring.io/projects/spring-boot). We use Apiary's Postgres backend. Make sure to have Docker installed and no existing Docker image running.


To start, clone Apiary from:  
https://github.com/DBOS-project/apiary.git

### Install Dependencies

The following comands are for Mac
To get started, let's first install some dependencies:

```shell
brew install openjdk@11
brew install maven
```

```shell
sudo ln -sfn /usr/local/opt/openjdk@11/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-11.jdk
```

### Compile Apiary

Next, let's compile Apiary. In the Apiary root directory, run:

```shell
mvn -DskipTests package
```

If running into this error:

```shell
[ERROR] Failed to execute goal org.xolstice.maven.plugins:protobuf-maven-plugin:0.6.1:compile (default) on project apiary: Unable to resolve artifact: Missing:
[ERROR] ----------
[ERROR] 1) com.google.protobuf:protoc:exe-x86_64:3.7.0
```

Inside of pom.xml in Apiary root directory:

Replace `${os.detected.classifier}` in `com.google.protobuf:protoc:3.7.0:exe:${os.detected.classifier}` with `osx-x86_64`.

### Start Postgres in Docker

Then, let's start Postgres using a Docker image. We recommend you [configure Docker](https://docs.docker.com/engine/install/linux-postinstall/) so it can be run by non-root users.

Ensure you configure the following script: [initialize_postgres_docker.sh](apiary/scripts/initialize_postgres_docker.sh).

Make the following changes to the configuration:

- **Database Connection (5432)**  
  PostgreSQL in Docker runs on port 5432 inside the container. Docker forwards port 5432 from the container to your local machine’s port 5432. Your Spring Boot app connects to PostgreSQL through localhost:5432.

- **Web Application (8081)**  
  Spring Boot runs on localhost:8081 on your local machine. Your web browser accesses the web application by going to localhost:8081. When the web application needs to query the database, it connects to localhost:5432 (PostgreSQL) to execute SQL queries.

Run the following command:

```shell
scripts/initialize_postgres_docker.sh
```

### Run the Website

To run the website, follow these steps:

```shell
mvn clean && mvn package && mvn spring-boot:run
```

Then, navigate to `localhost:8081` to view this new social network! You should see the Nectar homepage:

<img src="https://storage.googleapis.com/apiary_public/nectar_network_homepage.png" width="600">
