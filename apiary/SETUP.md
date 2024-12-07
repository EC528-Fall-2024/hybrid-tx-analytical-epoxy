# Apiary
Requires Java 11 and maven:

    sudo apt install openjdk-11-jdk maven libatomic1

Also requires VoltDB; set the following environment variables:

    export VOLT_HOME=/path/to/voltdb
    export PATH="$VOLT_HOME/bin:$PATH"

To initialize VoltDB, run its startup script:

    scripts/initialize_voltdb.sh

To compile and run unit tests:

    mvn package

## Quick Test with Docker
You could use Docker containers to deploy database servers and quickly test Apiary.

Install Docker:
```
sudo apt install docker.io
```

After installation, if you want to run `docker` command without sudo, please follow the instructions [here](https://docs.docker.com/engine/install/linux-postinstall/).

Start a VoltDB instance with Docker:
```
scripts/initialize_voltdb_docker.sh
```

Start a Postgres instance with Docker:
```
scripts/initialize_postgres_docker.sh
```

Start a Vertica instance with Docker:
```
scripts/initialize_vertica_docker.sh
```

Now you could run some Apiary unit tests!
```
mvn test
```
All should pass.

## Add SQL Data to Postgres

We have created three tables (`users`, `posts`, and `likes`) with dummy data to show an example of our epoxy being used on a mock social media application. Here is how to add this data to the docker file:

1. Ensure your docker file is initialized and running. To see if it is, you can run the Docker app or check on cli.

    ```
    docker ps
    ```

2. Run the `add_postgres_data.sh` file.

    ```
    scripts/add_postgres_data.sh
    ```

And you're done! You are now able to run the etl-demo on this propogated dummy data.


## Elasticsearch

Download the Elasticsearch binaries from [here](https://www.elastic.co/downloads/elasticsearch).

Set `ES_HOME` to your Elasticsearch root directory.

Set the password for the `elastic` user:
```
bin/elasticsearch-reset-password -u elastic -i
```
