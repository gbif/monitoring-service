package org.gbif.service.monitoring.model;


import java.util.Collection;

import lombok.Data;
import org.apache.curator.x.discovery.ServiceInstance;

@Data
public class Service {

  private final String name;
  private final Collection<ServiceInstance<Object>> instances;

  public boolean isAvailable() {
    return !(instances == null || instances.isEmpty());
  }



}
