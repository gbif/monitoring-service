package org.gbif.service.monitoring.resources;

import org.gbif.service.monitoring.model.Platform;
import org.gbif.service.monitoring.model.TreeNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class GraphController {

  @Autowired
  private EnvironmentsMonitorResource environmentsMonitorResource;

  //Jersey/Jackson objects
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @GetMapping(value = "/graph")
  public String getGraph(Model model){
    try {
      Map<String, Object> map = new HashMap<>();
      Platform platform = new Platform();
      platform.setName("GBIF");
      platform.setEnvironments(environmentsMonitorResource.getEnvironments());
      map.put("data", MAPPER.writeValueAsString(TreeNode.toD3Node(platform)));
      model.addAttribute("data", MAPPER.writeValueAsString(TreeNode.toD3Node(platform)));
      return "graph";
    } catch(IOException ex){
      throw new RuntimeException(ex);
    }
  }

  @GetMapping(value = "graph/{env}")
  public ModelAndView getGraph(@PathVariable("env") String env){
    try {
      Map<String, Object> map = new HashMap<>();
      map.put("data", MAPPER.writeValueAsString(TreeNode.toD3Node(environmentsMonitorResource.getEnvironment(env))));
      return new ModelAndView("/graph",map);
    } catch(IOException ex){
      throw new RuntimeException(ex);
    }
  }

  @GetMapping(value = "graph/{env}/{service}", produces = MediaType.TEXT_HTML_VALUE)
  public ModelAndView getGraph(@PathVariable("env") String env, @PathVariable("service") String serviceName){
    try {
      Map<String, Object> map = new HashMap<>();
      map.put("data", MAPPER.writeValueAsString(TreeNode.toD3Node(environmentsMonitorResource.getService(env, serviceName))));
      return new ModelAndView("/graph",map);
    } catch(IOException ex){
      throw new RuntimeException(ex);
    }
  }
}
