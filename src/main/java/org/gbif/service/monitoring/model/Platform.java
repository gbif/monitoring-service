package org.gbif.service.monitoring.model;

import java.util.List;

public class Platform {

  private String name;

  private List<Environment> environments;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Environment> getEnvironments() {
    return environments;
  }

  public void setEnvironments(List<Environment> environments) {
    this.environments = environments;
  }
}
