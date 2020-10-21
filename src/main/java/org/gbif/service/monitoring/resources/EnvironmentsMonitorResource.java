package org.gbif.service.monitoring.resources;

import org.gbif.service.monitoring.conf.MonitoringConfiguration;
import org.gbif.service.monitoring.model.Environment;
import org.gbif.service.monitoring.model.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Web resource that provides information about the status of GBIF services.
 * Allows to check the availability of services per environment and execute basic GET requests on them.
 */
@RestController
@RequestMapping(
  produces = {MediaType.APPLICATION_JSON_VALUE, "application/x-javascript"}
)
public class EnvironmentsMonitorResource {

  //Keeps a list if curator frameworks for each environment
  private final MonitoringConfiguration configuration;


  /**
   *  Default constructor: curators and settings, must be provided.
   */
  @Autowired
  public EnvironmentsMonitorResource(MonitoringConfiguration configuration){
    this.configuration = configuration;
  }

  /**
   * Gets the list of registered environments and status of GBIF services for each environment.
   */
  @GetMapping("environment")
  public List<Environment> getEnvironments() {
    return configuration.getCurators().entrySet().stream()
            .map(env -> getEnvironmentDetails(env.getKey()))
            .collect(Collectors.toList());
  }

  /**
   * Gets the information of GBIF service for a environment.
   */
  @GetMapping("environment/{env}")
  public Environment getEnvironment(@PathVariable("env") String env) {
    Environment.Type type = Environment.Type.valueOf(env.toUpperCase());
    return getEnvironmentDetails(type);
  }

  /**
   * Gets the status and running instances of a GBIF service in environment.
   */
  @GetMapping("environment/{env}/{service}")
  public Service getService(@PathVariable("env") String env, @PathVariable("service") String service) {
    Environment.Type type = Environment.Type.valueOf(env.toUpperCase());
     try (ServiceDiscovery discovery = discovery(type)) {
       return new Service(service.toLowerCase(), discovery.queryForInstances(service));
     } catch (Exception ex) {
       throw new RuntimeException(ex);
     }

  }

  /**
   * Gets the information of running instance of a service in an environment.
   */
  @GetMapping("environment/{env}/{service}/{instance}")
  public ServiceInstance<Object> getInstance(@PathVariable("env") String env, @PathVariable("service") String service, @PathVariable("instance") String instance) {
    Environment.Type type = Environment.Type.valueOf(env.toUpperCase());
    try (ServiceDiscovery<Object> discovery = discovery(type)) {
      return discovery.queryForInstance(service,instance);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }

  }

  /**
   * Gets an instance of a discovery service.
   */
  private ServiceDiscovery<Object> discovery(Environment.Type env) {
      CuratorFramework curator = configuration.getCurator(env);
      JsonInstanceSerializer<Object> serializer = new JsonInstanceSerializer<>(Object.class);
      return ServiceDiscoveryBuilder.builder(Object.class)
        .client(curator)
        .basePath(configuration.getCuratorConfiguration(env).getZkPath())
        .serializer(serializer)
        .build();
    }

  /**
   * Gets the details of environment: service name and running instances.
   */
  private Environment getEnvironmentDetails(Environment.Type type) {
    try (ServiceDiscovery<Object> discovery = discovery(type)) {
      return new Environment(type, discovery.queryForNames().stream()
        .map(
          serviceName -> {
            try {
              return new Service(serviceName, discovery.queryForInstances(serviceName));
            } catch(Exception ex){
              throw new RuntimeException(ex);
            }
          }
        )
        .collect(Collectors.toList()));
    } catch (Exception ex) {
     throw new RuntimeException(ex);
    }
  }
}
