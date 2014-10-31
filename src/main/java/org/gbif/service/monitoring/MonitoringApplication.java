package org.gbif.service.monitoring;

import org.gbif.service.monitoring.model.Environment;
import org.gbif.service.monitoring.resources.EnvironmentsMonitorResource;
import org.gbif.utils.file.properties.PropertiesUtil;
import org.gbif.ws.app.ConfUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.ws.rs.core.Application;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

/**
 * Jersey application that configure the environment for the EnvironmentsMonitorResource.
 */
public class MonitoringApplication extends Application {

  //Default name of application settings file
  private static final String APP_CONF_FILE = "application.properties";

  //Pattern of the ZkHost for each environment
  private static final String ZKHOST_FMT = "zkhost_%s";

  //Loads the settings file
  private static final Properties CONF = loadConfiguration();

  //Loads the curators framework for each environment
  private Map<Environment.Type, CuratorFramework> curators = loadCurators(CONF);

  //Configure the resource as a singleton.
  @Override
  public Set<Object> getSingletons() {
    return new ImmutableSet.Builder<Object>().add(new EnvironmentsMonitorResource(curators,CONF)).build();
  }

  /**
   * Loads the configuration file.
   */
  private static Properties loadConfiguration() {
    try {
      return PropertiesUtil.readFromFile(ConfUtils.getAppConfFile(APP_CONF_FILE));
    } catch (IOException ex) {
      Throwables.propagate(ex);
    }
    throw new IllegalStateException();
  }

  /**
   * Builds a new instance of a CuratorFramework client for each registered environment.
   */
  private static Map<Environment.Type, CuratorFramework> loadCurators(Properties properties) {
    ImmutableMap.Builder<Environment.Type, CuratorFramework> curatorsBuilder = new ImmutableMap.Builder<Environment.Type, CuratorFramework>();
    for (Environment.Type envType : Environment.Type.values()) {
      String envZkHost = String.format(ZKHOST_FMT, envType.name().toLowerCase());
      if (properties.containsKey(envZkHost)) {
        curatorsBuilder.put(envType, curator(properties.getProperty(envZkHost)));
      }
    }
    return curatorsBuilder.build();
  }

  /**
   * Instantiates a Curator read-only client.
   */
  private static CuratorFramework curator(String zkHost) {
    CuratorFramework curator = CuratorFrameworkFactory.builder()
      .connectString(zkHost)
      .retryPolicy(new RetryNTimes(5,1000))
      .canBeReadOnly(true)
      .build();
    curator.start();
    return curator;
  }

}
