package org.gbif.service.monitoring.resources;

import org.gbif.service.monitoring.model.TreeNode;
import org.gbif.service.monitoring.model.Environment;
import org.gbif.service.monitoring.model.InstanceResponse;
import org.gbif.service.monitoring.model.Platform;
import org.gbif.service.monitoring.model.Service;
import org.gbif.ws.discovery.conf.ServiceDetails;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.view.Viewable;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Web resource that provides information about the status of GBIF services.
 * Allows to check the availability of services per environment and execute basic GET requests on them.
 */
@Produces(MediaType.APPLICATION_JSON)
@Path("/")
public class EnvironmentsMonitorResource {

  //Pattern of Zk path to discovery service namespace of an environment
  private static final String ZK_SERVICES_PATH_FMT = "zkservicespath_%s";

  //Jersey/Jackson objects
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final JsonFactory JSON_FACTORY = MAPPER.getJsonFactory();
  private static final Client JERSEY_CLIENT = Client.create();

  //Keeps a list if curator frameworks for each environment
  private final Map<Environment.Type, CuratorFramework> curators;

  //Properties file
  private final Properties settings;

  //Transform a ClientResponse into server Response.
  private static Response clientResponseToResponse(ClientResponse r) throws IOException {
    // copy the status code
    Response.ResponseBuilder rb = Response.status(r.getStatus());
    // copy all the headers
    for (Map.Entry<String, List<String>> entry : r.getHeaders().entrySet()) {
      for (String value : entry.getValue()) {
        rb.header(entry.getKey(), value);
      }
    }
    //Parse the response entity as JsonNode
    JsonParser jp = JSON_FACTORY.createJsonParser(r.getEntity(new GenericType<String>() {}));
    JsonNode actualObj = MAPPER.readTree(jp);
    rb.entity(actualObj);
    // return the response
    return rb.build();
  }

  /**
   * Take a list of available instances and add the services not listed in it.
   */
  private static ImmutableList<Service> addUnAvailableServices(List<Service> availableServices){
    ImmutableList.Builder<Service> services = new ImmutableList.Builder<Service>();
    for(final String serviceName : Service.REGISTERED_SERVICES){
      Optional<Service> serviceOpt = Iterables.tryFind(availableServices, new Predicate<Service>() {
        @Override
        public boolean apply(Service input) {
          return serviceName.equalsIgnoreCase(input.getName());
        }
      });
      if(!serviceOpt.isPresent()) {
        services.add(new Service(serviceName));
      }
    }
    return services.build();
  }

  /**
   *  Default constructor: curators and settings, must be provided.
   */
  public EnvironmentsMonitorResource(Map<Environment.Type, CuratorFramework> curators, Properties settings){
    this.curators = curators;
    this.settings = settings;
  }

  /**
   * Gets the list of registered environments and status of GBIF services for each environment.
   */
  @GET
  @Path("environment")
  public List<Environment> getEnvironments() {
    ImmutableList.Builder<Environment> environments = new ImmutableList.Builder<Environment>();
    for(Environment.Type environmentType : Environment.Type.values()){
      if(curators.containsKey(environmentType)) {
        environments.add(getEnvironmentDetails(environmentType));
      }
    }
    return environments.build();
  }

  /**
   * Gets the information of GBIF service for a environment.
   */
  @GET
  @Path("environment/{env}")
  public Environment getEnvironment(@PathParam("env") String env) {
    final Optional<Environment.Type> type = Enums.getIfPresent(Environment.Type.class, env.toUpperCase());
    if (type.isPresent()) {
      return getEnvironmentDetails(type.get());
    }
    throw new WebApplicationException(Response.Status.NOT_FOUND);
  }

  /**
   * Gets the status and running instances of a GBIF service in environment.
   */
  @GET
  @Path("environment/{env}/{service}")
  public Service getService(@PathParam("env") String env, @PathParam("service") String service) {
    Optional<Environment.Type> type = Enums.getIfPresent(Environment.Type.class, env.toUpperCase());
    if (type.isPresent() && Service.REGISTERED_SERVICES.contains(service.toLowerCase())) {
       try (ServiceDiscovery discovery = discovery(type.get())) {
         return new Service(service.toLowerCase(), Lists.newArrayList(discovery.queryForInstances(service)));
       } catch (Exception ex) {
         throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
       }
    }
    throw new WebApplicationException(Response.Status.NOT_FOUND);
  }

  /**
   * Gets the information of running instance of a service in an environment.
   */
  @GET
  @Path("environment/{env}/{service}/{instance}")
  public ServiceInstance<ServiceDetails> getInstance(@PathParam("env") String env, @PathParam("service") String service, @PathParam("instance") String instance) {
    Optional<Environment.Type> type = Enums.getIfPresent(Environment.Type.class, env.toUpperCase());
    if (type.isPresent() && Service.REGISTERED_SERVICES.contains(service.toLowerCase())) {
      try (ServiceDiscovery<ServiceDetails> discovery = discovery(type.get())) {
        return discovery.queryForInstance(service,instance);
      } catch (Exception ex) {
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }
    }
    throw new WebApplicationException(Response.Status.NOT_FOUND);
  }

  /**
   * Propagates a GET request to each instance of services.
   */
  @GET
  @Path("proxy/{env}/{service}")
  public List<InstanceResponse> proxy(@PathParam("env") String env, @PathParam("service") String service, @QueryParam("path") String path, @Context
  UriInfo uriInfo) {
    Optional<Environment.Type> type = Enums.getIfPresent(Environment.Type.class, env.toUpperCase());
    List<InstanceResponse> responses = Lists.newArrayList();
    if (type.isPresent() && Service.REGISTERED_SERVICES.contains(service.toLowerCase())) {
      try (ServiceDiscovery<ServiceDetails> discovery = discovery(type.get())) {
        for(ServiceInstance<ServiceDetails> serviceInstance  : discovery.queryForInstances(service)) {
          final URL url = UriBuilder.fromPath(serviceInstance.getPayload().getExternalUrl().concat(path)).buildFromMap(uriInfo.getQueryParameters()).toURL();
          final ClientResponse clientResponse = JERSEY_CLIENT.resource(url.toURI()).accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
          responses.add(new InstanceResponse(serviceInstance.getId(),clientResponseToResponse(clientResponse)));
        }
      } catch (Exception ex) {
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }
    }
    return responses;
  }

  @GET
  @Path("graph")
  @Produces(MediaType.TEXT_HTML)
  public Viewable getGraph(){
    try {
      Map<String, Object> map = new HashMap<>();
      Platform platform = new Platform();
      platform.setName("GBIF");
      platform.setEnvironments(getEnvironments());
      map.put("data",MAPPER.writeValueAsString(TreeNode.toD3Node(platform)));
      return new Viewable("/graph",map);
    } catch(IOException ex){
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @GET
  @Path("graph/{env}")
  @Produces(MediaType.TEXT_HTML)
  public Viewable getGraph(@PathParam("env") String env){
    try {
      Map<String, Object> map = new HashMap<>();
      map.put("data",MAPPER.writeValueAsString(TreeNode.toD3Node(getEnvironment(env))));
      return new Viewable("/graph",map);
    } catch(IOException ex){
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @GET
  @Path("graph/{env}/{service}")
  @Produces(MediaType.TEXT_HTML)
  public Viewable getGraph(@PathParam("env") String env, @PathParam("service") String serviceName){
    try {
      Map<String, Object> map = new HashMap<>();
      map.put("data",MAPPER.writeValueAsString(TreeNode.toD3Node(getService(env, serviceName))));
      return new Viewable("/graph",map);
    } catch(IOException ex){
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Gets an instance of a discovery service.
   */
  private ServiceDiscovery<ServiceDetails> discovery(Environment.Type env) {
      CuratorFramework curator = curators.get(env);
      JsonInstanceSerializer<ServiceDetails> serializer = new JsonInstanceSerializer<ServiceDetails>(ServiceDetails.class);
      return ServiceDiscoveryBuilder.builder(ServiceDetails.class)
        .client(curator)
        .basePath(settings.getProperty(String.format(ZK_SERVICES_PATH_FMT, env.name().toLowerCase())))
        .serializer(serializer)
        .build();
    }

  /**
   * Gets the details of environment: service name and running instances.
   */
  private Environment getEnvironmentDetails(Environment.Type type) {
    try (ServiceDiscovery<ServiceDetails> discovery = discovery(type)) {
      ImmutableList.Builder<Service> servicesBuilder = new ImmutableList.Builder<Service>();
      for (String serviceName : discovery.queryForNames()) {
        servicesBuilder.add(new Service(serviceName,Lists.newArrayList(discovery.queryForInstances(serviceName))));
      }
      //Adds the services that aren't running/registered
      return new Environment(type,servicesBuilder.addAll(addUnAvailableServices(servicesBuilder.build())).build());
    } catch (Exception ex) {
      Throwables.propagate(ex);
    }
    throw new IllegalStateException();
  }
}
