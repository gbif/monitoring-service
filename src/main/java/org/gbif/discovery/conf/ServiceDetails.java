package org.gbif.discovery.conf;

import com.google.common.base.Objects;
import lombok.Data;

@Data
public class ServiceDetails {

  private static final String URL_FMT = "http://%s:%s/";
  private String groupId;
  private String artifactId;
  private String version;
  private ServiceConfiguration serviceConfiguration;
  private ServiceStatus status;

  public String getFullName() {
    return this.artifactId + '-' + this.version;
  }

  public String getExternalUrl() {
    return String.format("http://%s:%s/", this.serviceConfiguration.getHost(), Objects.firstNonNull(this.serviceConfiguration.getExternalPort(), this.serviceConfiguration.getHttpPort()));
  }
}
