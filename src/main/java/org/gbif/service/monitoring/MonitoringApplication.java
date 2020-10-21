package org.gbif.service.monitoring;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.zookeeper.ZookeeperAutoConfiguration;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryAutoConfiguration;
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryClientConfiguration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperAutoServiceRegistrationAutoConfiguration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperServiceRegistryAutoConfiguration;
import org.springframework.cloud.zookeeper.support.CuratorServiceDiscoveryAutoConfiguration;
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
@SpringBootApplication(exclude = {ZookeeperAutoConfiguration.class, CuratorServiceDiscoveryAutoConfiguration.class,
  ZookeeperDiscoveryClientConfiguration.class, ZookeeperAutoServiceRegistrationAutoConfiguration.class,
  ZookeeperDiscoveryAutoConfiguration.class, ZookeeperServiceRegistryAutoConfiguration.class})
public class MonitoringApplication extends SpringBootServletInitializer {

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    return builder.sources(MonitoringApplication.class);
  }

  public static void main(String[] args) {
    SpringApplication.run(MonitoringApplication.class, args);
  }

}
