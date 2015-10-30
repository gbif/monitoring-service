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
 
## How to use this application
This application exposes two basic services described below:
 
### Graph service
This service shows a graphic view of services, it is accessible trough a web browser at he following URLs:

  * `{server}:{httpPort}/graph`: shows a graph of all the environments
  * `{server}:{httpPort}/graph/{environment}`: shows a graph of an environment (dev, uat or prod)
  * `{server}:{httpPort}/graph/{environment}/{service}`: shows a graph of an environment (dev, uat or prod)

### Json service
This service shows information in JSON format of services, it is accessible trough a web browser at he following URLs:
    
  * `{server}:{httpPort}/{environment}`: services of an environment (dev, uat or prod)
  * `{server}:{httpPort}/{environment}/{service}`:  service detail 
  * `{server}:{httpPort}/{environment}/{service}/{instance}`:  service instance details, (instance: service instance id) 
