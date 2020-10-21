package org.gbif.service.monitoring.conf;


import java.util.Objects;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class CuratorConfiguration {

  @NonNull
  private String zkHost;

  @NonNull
  private String zkPath;

  private CuratorFramework curator;

  /**
   * Instantiates a Curator read-only client.
   */
  public CuratorFramework getCurator() {
    if (Objects.isNull(curator)) {
      curator = CuratorFrameworkFactory.builder()
        .connectString(zkHost)
        .retryPolicy(new RetryNTimes(5, 1000))
        .canBeReadOnly(true)
        .build();
      curator.start();
    }
    return curator;
  }

}
