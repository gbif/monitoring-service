package org.gbif.service.monitoring.resources;

import org.gbif.service.monitoring.model.Platform;
import org.gbif.service.monitoring.model.TreeNode;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class GraphController {

  @Autowired
  private EnvironmentsMonitorResource environmentsMonitorResource;

  //Jersey/Jackson objects
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @GetMapping(value = "/graph")
  public String getGraph(Model model) {
    try {
      Platform platform = new Platform();
      platform.setName("GBIF");
      platform.setEnvironments(environmentsMonitorResource.getEnvironments());
      model.addAttribute("data", MAPPER.writeValueAsString(TreeNode.toD3Node(platform)));
      return "graph";
    } catch(IOException ex){
      throw new RuntimeException(ex);
    }
  }

  @GetMapping(value = "graph/{env}")
  public String getGraph(@PathVariable("env") String env, Model model) {
    try {
      model.addAttribute("data", MAPPER.writeValueAsString(TreeNode.toD3Node(environmentsMonitorResource.getEnvironment(env))));
      return "graph";
    } catch(IOException ex){
      throw new RuntimeException(ex);
    }
  }

  @GetMapping(value = "graph/{env}/{service}")
  public String getGraph(@PathVariable("env") String env, @PathVariable("service") String serviceName, Model model) {
    try {
      model.addAttribute("data", MAPPER.writeValueAsString(TreeNode.toD3Node(environmentsMonitorResource.getService(env, serviceName))));
      return "graph";
    } catch(IOException ex){
      throw new RuntimeException(ex);
    }
  }
}
