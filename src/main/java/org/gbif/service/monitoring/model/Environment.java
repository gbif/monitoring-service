package org.gbif.service.monitoring.model;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;

/**
 * GBIF deployment environment.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Environment {

  /**
   * Environment type.
   */
  public enum Type {
    DEV, UAT, PROD
  }

  private Type type;

  private List<Service> services;

  /**
   * Default constructor.
   */
  public Environment(Type type, List<Service> services) {
    this.type = type;
    this.services = services;
  }

  /**
   * Environment type.
   */
  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  /**
   * Registered services for this environment.
   * This list can't be empty, if the service is unavailable it must be provided anyway.
   */
  public List<Service> getServices() {
    return services;
  }

  public void setServices(List<Service> services) {
    this.services = services;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("type", type).add("services", services).toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Environment that = (Environment) o;

    return Objects.equal(this.type, that.type) && Objects.equal(this.services, that.services);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(type, services);
  }

}
