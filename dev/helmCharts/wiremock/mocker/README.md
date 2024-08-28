
# Mock Data Catalog, Schema Registry and PMSC

The mocked data is stored under directory `data` and Mocker can be started in following way.

## Using Wiremock standalone

1. Donwload [Wiremock Standalone JAR](https://wiremock.org/docs/running-standalone/).

2. Copy the wiremock jar into **_mocker_** directory:

   ```
   -rw-r--r--@ 1 <USERNAME>  staff  15734816  9 Nov 14:13 wiremock-jre8-standalone-2.35.0.jar
   drwxr-xr-x  4 <USERNAME>  staff       128  9 Nov 14:55 data
   -rw-r--r--  1 <USERNAME>  staff       746 10 Nov 15:44 README.md

   ```

3. Start Wiremock server in **_mocker_** directory.
   ```
   java -jar wiremock-jre8-standalone-2.35.0.jar --root-dir ./data

   ```

   Or you can leave the wiremock jar in other directory and run the jar as below:

   ```
   java -jar <wiremock jar path> ./data

   ```

## Using Wiremock in Docker or Podman:

```
docker run -it --rm -p 8080:8080 --name wiremock -v $PWD/data:/home/wiremock wiremock/wiremock:2.34.0

podman run -it --rm -p 8080:8080 --name wiremock -v $PWD/data:/home/wiremock wiremock/wiremock:2.34.0
```
