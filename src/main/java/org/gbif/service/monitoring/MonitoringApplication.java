package org.gbif.service.monitoring;


import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * Jersey application that configure the environment for the EnvironmentsMonitorResource.
 */
@EnableConfigurationProperties
@ComponentScan(
  basePackages = {
    "org.gbif.service.monitoring.resources",
    "org.gbif.service.monitoring.conf"
  }
)
@SpringBootApplication
@EnableDiscoveryClient
@EnableAdminServer
public class MonitoringApplication {

  public static void main(String[] args) {
    SpringApplication.run(MonitoringApplication.class, args);
  }

}
