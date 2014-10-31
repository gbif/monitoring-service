package org.gbif.service.monitoring.model;

import org.gbif.ws.discovery.conf.ServiceDetails;

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import org.apache.curator.x.discovery.ServiceInstance;

public class Service {

  //List of GBIF services that can be registered in Zookeeper.
  public static List<String> REGISTERED_SERVICES = new ImmutableList.Builder<String>().add("occurrence-ws")
    .add("registry-ws")
    .add("checklistbank-ws")
    .add("crawler-ws")
    .add("metrics-ws")
    .add("tile-server")
    .add("image-cache")
    .add("geocode-ws")
    .build();

  private String name;
  private List<ServiceInstance<ServiceDetails>> instances;



  public Service(String name){
    this.name = name;
  }

  public Service(String name, List<ServiceInstance<ServiceDetails>> instances){
    this.name = name;
    this.instances = instances;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isAvailable() {
    return !(instances == null || instances.isEmpty());
  }

  public List<ServiceInstance<ServiceDetails>> getInstances() {
    return instances;
  }

  public void setInstances(List<ServiceInstance<ServiceDetails>> instances) {
    this.instances = instances;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name, instances);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Service that = (Service) o;

    return Objects.equal(this.name, that.name) &&
           Objects.equal(this.instances, that.instances);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("name", name)
      .add("instances", instances)
      .toString();
  }

}
