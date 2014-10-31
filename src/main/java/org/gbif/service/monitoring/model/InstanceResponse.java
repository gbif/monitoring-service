package org.gbif.service.monitoring.model;

import javax.ws.rs.core.Response;

import com.google.common.base.Objects;

/**
 * Response of service instance.
 * This class is used for the 'proxy' service to represent the response of running instance.
 */
public class InstanceResponse {

  private String instanceId;

  private Response response;

  /**
   * Full constructor.
   */
  public InstanceResponse(String instanceId, Response response){
    this.instanceId = instanceId;
    this.response = response;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public Response getResponse() {
    return response;
  }

  public void setResponse(Response response) {
    this.response = response;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("instanceId", instanceId).add("response", response).toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    InstanceResponse that = (InstanceResponse) o;

    return Objects.equal(this.instanceId, that.instanceId) && Objects.equal(this.response, that.response);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(instanceId, response);
  }
}
