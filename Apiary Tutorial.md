# New Apiary Tutorial and Demo

This tutorial will show you how to build a simple social network web application using Apiary and [Spring Boot](https://spring.io/projects/spring-boot). We use Apiary's Postgres backend. Make sure to have Docker installed and no existing Docker image running.

To start, clone Apiary to your local environment:

    git clone https://github.com/DBOS-project/apiary.git
    cd apiary

### Install Dependencies

1. **Install Java/OpenJDK:**

    - For Mac/Linux:

          brew install openjdk@11

    - For Windows:

      - Install JDK using [this link](https://adoptium.net/).
      - Set up the environmental variable for Java/OpenJDK:
        
            setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-21.0.4.7-hotspot"

      Verify the installation:

          java -version

2. **Install Maven:**

    - For Mac/Linux:

          brew install maven

    - For Windows:

      - Install Maven using [this link](https://maven.apache.org/download.cgi).
      - Set up the environmental variables for Maven:

            setx MAVEN_HOME "C:\Program Files\Apache\Maven"
            setx PATH "%MAVEN_HOME%\bin;%PATH%"

      Verify the installation:

          mvn -v

### Compile Apiary

Next, let's compile Apiary.

- For Mac/Linux:

  - In the Apiary root directory, run:

        mvn -DskipTests package

  If you run into this error:

      [ERROR] Failed to execute goal org.xolstice.maven.plugins:protobuf-maven-plugin:0.6.1:compile (default) on project apiary: Unable to resolve artifact: Missing:
      [ERROR] ----------
      [ERROR] 1) com.google.protobuf:protoc:exe-x86_64:3.7.0

  Inside of pom.xml in Apiary root directory:

  Replace `${os.detected.classifier}` in `com.google.protobuf:protoc:3.7.0:exe:${os.detected.classifier}` with `osx-x86_64`.

- For Windows:

  If the OpenJDK installation went smoothly, then there's no need to do anything.

### Start Postgres in Docker

Then, let's start Postgres using a Docker image. We recommend you [configure Docker](https://docs.docker.com/engine/install/linux-postinstall/) so it can be run by non-root users.

Ensure you configure the following script: [initialize_postgres_docker.sh](scripts/initialize_postgres_docker.sh).

Make the following changes to the configuration since docker for these OS runs on lightweight VM:

- **Database Connection (5432)**  
  PostgreSQL in Docker runs on port 5432 inside the container. Docker forwards port 5432 from the container to your local machine’s port 5432. Your Spring Boot app connects to PostgreSQL through localhost:5432.

- **Web Application (8081)**  
  Spring Boot runs on localhost:8081 on your local machine. Your web browser accesses the web application by going to localhost:8081. When the web application needs to query the database, it connects to localhost:5432 (PostgreSQL) to execute SQL queries.


For Mac/Linux:

  - Run the following command:

        scripts/initialize_postgres_docker.sh

For Windows:

  - Download [Git](https://git-scm.com/downloads) or [WSL](https://learn.microsoft.com/en-us/windows/wsl/install). Open either **Git Bash** or **WSL**
  - In the Apiary root directory, run:

        bash scripts/initialize_postgres_docker.sh


### Run the Postgres Demo Website

To run the postgres-demo website, follow these steps:


  1. Run spring-boot from the Apiary root directory:
  
          mvn clean && mvn package && mvn spring-boot:run

      or:

          mvn clean
          mvn package
          mvn spring-boot:run

      If this doesn't work, add the following code to [pom.xlm](pom.xlm), and try the above commands again:

          <plugin>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-maven-plugin</artifactId>
              <configuration>
                  <mainClass>org.dbos.apiary.worker.ApiaryWorkerExecutable</mainClass>
              </configuration>
          </plugin>
  
  2. Run spring-boot from postgres-demo:

      Run the same mvn commands as above.

  
If you run into this error:

    [ERROR] Tests run: 1, Failures: 0, Errors: 1, Skipped: 0, Time elapsed: 4.176 s <«< FAILUREl - in org.dbos.apiary•postgresdemo.WebsiteApplicationTests
    [ERROR]contextLoads  Time elapsed: 0.016 s くくくERROR！
    java.lang. IllegalStateException: Failed to load ApplicationContext
    Caused by:org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'nectarController' defined in file [<your_path>/apiary/postgres-demo/target/classes/org/dbos/apiary/postgresdemo/NectarController.class]: Instantiation of bean failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [org.dbos.apiary.postgresdemo.NectarController): Constructor threw exception; nested exception is java.lang.RuntimeException: Failed to connect to Postgres
    Caused by: org.springframework.beans.BeanInstantiationException: Failed to instantiate [org.dbos.apiary-postgresdemo.NectarController]: Constructor threw exce ption;
    nested exception is java.lang. RuntimeException: Failed to connect to Postgres
    Caused by: java.lang.RuntimeException: Failed to connect to Postgres


    [ERROR] Errors:
    [ERROR]  WebsiteApplicationTests.contextLoads » IllegalState Failed to load

Try following steps:

  1. Delete "postgres" image in docker images, and remove "apiary-postgres" container from containers. 

  2. Check whether local port 5432 and 8081 is available for building connection by:

          lsof -i :8081
          lsof -i :5432
    
     This will list any processes that are currently using those ports. If there is no output, that means nothing is using the port.
       - If there are outputs, it might look like this:
         
              COMMAND   PID   USER   FD   TYPE    DEVICE SIZE/OFF NODE NAME
                java     1234  user   67u  IPv6    0x12345678      0t0  TCP *:8081 (LISTEN)
         
       - You can kill the process by specifying the PID. This will forcefully stop the process and free the port.
         
              kill -9 <PID>
         
  3. Double-Check the Ports. After killing the process, you can re-run the ```lsof -i :8081``` and ```lsof -i :5432``` commands to make sure no processes are using the ports anymore.


Then, navigate to `localhost:8081` to view this new social network! You should see the Nectar homepage:

<img src="https://storage.googleapis.com/apiary_public/nectar_network_homepage.png" width="600">