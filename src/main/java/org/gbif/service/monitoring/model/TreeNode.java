package org.gbif.service.monitoring.model;

import org.gbif.discovery.conf.ServiceDetails;

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.curator.x.discovery.ServiceInstance;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Class that represents a Node in a tree.
 * Used to display the deployment information in graphical view.
 */
public class TreeNode {

  //Jersey/Jackson objects
  private static final ObjectMapper MAPPER = new ObjectMapper();

  //Type of Node
  public enum NodeType {PLATFORM, ENVIRONMENT, SERVICE, SERVICE_INSTANCE, PORT}

  private String name;

  private List<TreeNode> children;

  private NodeType type;

  private JsonNode data;

  /**
   * Node name.
   */
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * List dependant nodes.
   */
  public List<TreeNode> getChildren() {
    return children;
  }

  public void setChildren(List<TreeNode> children) {
    this.children = children;
  }

  /**
   * Type of Node.
   */
  public NodeType getType() {
    return type;
  }

  public void setType(NodeType type) {
    this.type = type;
  }

  /**
   *Arbitrary JSON data to show detailed information about this node.
   */
  public JsonNode getData() {
    return data;
  }

  public void setData(JsonNode data) {
    this.data = data;
  }

  /**
   * Defines if the node is available or not.
   * PLATFORM and ENVIRONMENT nodes are available if all its children are available.
   * SERVICE and SERVICE_INSTANCE are available if it contains children.
   * PORT are always available if it present.
   */
  public boolean isAvailable() {
    if(NodeType.PLATFORM == type || NodeType.ENVIRONMENT == type) {
      //an environment/platform is entirely available if all its service are available too
      return children != null && Iterables.tryFind(children, new Predicate<TreeNode>() {
        @Override
        public boolean apply(TreeNode input) {
          return input.isAvailable();
        }
      }).isPresent();
    } else if (NodeType.PORT != type) { //ports don't have children
      return children != null && !children.isEmpty();
    }
    //it's a port
    return true;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("name", name)
      .add("children", children)
      .add("type", type)
      .add("data", data)
      .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TreeNode that = (TreeNode) o;

    return Objects.equal(this.name, that.name) &&
           Objects.equal(this.children, that.children) &&
           Objects.equal(this.type, that.type) &&
           Objects.equal(this.data, that.data);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name, children, type, data);
  }

  /**
   * Converts a Platform to a TreeNode.
   */
  public static TreeNode toD3Node(Platform platform){
    TreeNode platformNode = new TreeNode();
    platformNode.setName(platform.getName());
    platformNode.setType(NodeType.PLATFORM);
    platformNode.setData(MAPPER.convertValue(platform, JsonNode.class));
    ImmutableList.Builder<TreeNode> children = new ImmutableList.Builder<TreeNode>();
    for(Environment environment : platform.getEnvironments()){
      children.add(toD3Node(environment));
    }
    platformNode.setChildren(children.build());
    return platformNode;
  }

  /**
   * Converts an Environment to a TreeNode.
   */
  public static TreeNode toD3Node(Environment environment){
   TreeNode envNode = new TreeNode();
   envNode.setName(environment.getType().name().toLowerCase());
   envNode.setType(NodeType.ENVIRONMENT);
   envNode.setData(MAPPER.convertValue(environment, JsonNode.class));
   ImmutableList.Builder<TreeNode> children = new ImmutableList.Builder<TreeNode>();
   for(Service service : environment.getServices()){
     children.add(toD3Node(service));
   }
   envNode.setChildren(children.build());
   return envNode;
  }

  /**
   * Converts an Service to a TreeNode.
   */
  public static TreeNode toD3Node(Service service) {
    TreeNode serviceNode = new TreeNode();
    serviceNode.setName(service.getName());
    serviceNode.setType(NodeType.SERVICE);
    serviceNode.setData(MAPPER.convertValue(service, JsonNode.class));
    ImmutableList.Builder<TreeNode> children = new ImmutableList.Builder<>();
    if(service.getInstances() != null) {
      for (ServiceInstance<ServiceDetails> serviceInstance : service.getInstances()) {
        children.add(toD3Node(serviceInstance));
      }
    }
    serviceNode.setChildren(children.build());
    return serviceNode;
  }

  /**
   * Converts an Service to a TreeNode.
   * Ports will be listed as its children.
   */
  public static TreeNode toD3Node(ServiceInstance<ServiceDetails> serviceInstance) {
    TreeNode serviceInstanceNode = new TreeNode();
    serviceInstanceNode.setName(serviceInstance.getId());
    serviceInstanceNode.setType(NodeType.SERVICE_INSTANCE);
    serviceInstanceNode.setData(MAPPER.convertValue(serviceInstance,JsonNode.class));
    ImmutableList.Builder<TreeNode> children = new ImmutableList.Builder<TreeNode>();
    children.add(portToD3Node(serviceInstance.getPayload().getServiceConfiguration().getExternalPort()));
    children.add(portToD3Node(serviceInstance.getPayload().getServiceConfiguration().getExternalAdminPort()));
    serviceInstanceNode.setChildren(children.build());
    return serviceInstanceNode;
  }

  /**
   * Converts port/Integer into a TreeNode.
   */
  public static TreeNode portToD3Node(Integer port){
    TreeNode portNode = new TreeNode();
    portNode.setName(":" + port.toString());
    portNode.setType(NodeType.PORT);
    portNode.setData(MAPPER.convertValue(port,JsonNode.class));
    return portNode;
  }
}
