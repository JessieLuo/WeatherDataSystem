# Distributed Weather Data system

## Overview

## Execution Instruction

### Use Maven `mvn` to execute

- cd RESTful
- `mvn clean package`
- Execute the `AggregationServer`:
  - `mvn exec:java -Dexec.mainClass="weather.server.AggregationServer" -Dexec.args="localhost 4567"`
- Execute the PUT request `ContentServer`:
  - `mvn exec:java -Dexec.mainClass="weather.server.AggregationServer" -Dexec.args="localhost 4567 Data/weatherTest.txt"`
- Execute the GET request `GETClient`:
  - `mvn exec:java -Dexec.mainClass="weather.client.GETClient" -Dexec.args="localhost 4567"`

### Compiled jar by maven -- **Recommend Way**

- cd RESTful
- `mvn clean package`
- cd target/
- move the jar file to parent folder which the `libs/` folder placed. The project compile and execution need those libs
- Start the `AggregationServer`:
  - `java -cp "libs/*:WeatherSystem.jar" weather.server.AggregationServer localhost 4567`
- Start execute the PUT requests `ContentServer`:
  - `java -cp "libs/*:WeatherSystem.jar" weather.Content.ContentServer localhost 4567 Data/weatherTest.txt`
- Start execute the GET requests `GETClient`:
  - `java -cp "libs/*:WeatherSystem.jar" weather.client.GETClient localhost 4567`

## Essential Notice

There exist two same name folder data which provide file access for both jar file and maven project
