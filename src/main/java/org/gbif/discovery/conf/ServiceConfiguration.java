package org.gbif.discovery.conf;

import com.google.common.base.Strings;
import lombok.Data;

@Data
public class ServiceConfiguration {

  private int httpPort;

  private int httpAdminPort;

  private String zkHost;

  private String zkPath;

  private String stopSecret;

  private Long timestamp;

  private Integer externalPort;

  private Integer externalAdminPort;

  private String host;

  private String containerName;

  private Integer maxRequestHeaderSize;

  private String conf;

  public boolean isDiscoverable() {
    return !Strings.isNullOrEmpty(this.zkHost) && !Strings.isNullOrEmpty(this.zkPath);
  }
}