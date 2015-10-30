#monitoring-service

Web application to monitor the status of GBIF environments: dev, uat and prod.
This applications exposes web services and web page to consult the status of services and instances of those.

## Configuration
An properties must be used to execute the application, the properties file must contains the zookeeper ensembles and
paths for each environment that will be monitored. The configuration keys follow the pattern ```zkhost_{environment}```
and ```zkservicespath_{environment}``` where 'environment' could be dev, uat or pro; the following example shows the 
configuration for the development environment 'dev':


```zkhost_dev=10.1.10.4:2181```

```zkservicespath_dev=/dev/services```


## Building the project

```mvn clean package -U```


## Executing the application

This application uses the library [gbif-microservice](https://github.com/gbif/gbif-microservice) which means that 
embeds an Jetty server and accepts the following parameters:
 
  * httpPort: http port that accepts regular request 
  * httpAdminPort: admin port to stop the service 
  * stopSecret: stop password 
  * conf: path to the configuration file (this [file](https://raw.githubusercontent.com/gbif/gbif-configuration/master/monitoring-service/application.properties) contains the configurations settings for all the GBIF environments)
   
 Example:
 ```java -jar target/monitoring-service-0.2-SNAPSHOT.jar -httpPort 8084 -httpAdminPort 8085 -stopSecret stop  -conf target/classes/application.properties```
