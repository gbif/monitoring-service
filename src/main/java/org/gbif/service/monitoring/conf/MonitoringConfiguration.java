package org.gbif.service.monitoring.conf;

import org.gbif.service.monitoring.model.Environment;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties
public class MonitoringConfiguration {

  private Map<Environment.Type, CuratorConfiguration> curators;

  public CuratorFramework getCurator(Environment.Type type) {
    return curators.get(type).getCurator();
  }

  public CuratorConfiguration getCuratorConfiguration(Environment.Type type) {
    return curators.get(type);
  }

}
