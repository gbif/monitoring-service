<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html;charset=utf-8"/>
  <script src="http://d3js.org/d3.v3.min.js" charset="utf-8"></script>

  <style type="text/css">

    body {
      overflow: hidden;
      margin: 0;
      font-size: 14px;
      font-family: "Helvetica Neue", Helvetica;
    }

    #chart, #header, #footer {
      position: absolute;
      top: 0;
    }

    #header, #footer {
      z-index: 1;
      display: block;
      font-size: 36px;
      font-weight: 300;
      text-shadow: 0 1px 0 #fff;
    }

    #header.inverted, #footer.inverted {
      color: #fff;
      text-shadow: 0 1px 4px #000;
    }

    #header {
      top: 80px;
      left: 70px;
      width: 1000px;
    }

    #footer {
      top: 680px;
      right: 140px;
      text-align: right;
    }

    .details {
      position: absolute;
      right: 0;
      font-size: 10px;
      border: 1px solid #a1a1a1;
      padding: 10px 40px;
      background: #fffff;
      max-height: 800px;
      width: 300px;
      border-radius: 5px;
      margin: 20px 10px;
    }

    .hint {
      position: absolute;
      width: 1280px;
      font-size: 12px;
      color: #999;
    }

    .hint-top {
      position: absolute;
      right: 0;
      width: 390px;
      font-size: 12px;
      color: #999;
    }

    .node circle {
      cursor: pointer;
      fill: #fff;
      stroke: steelblue;
      stroke-width: 1.5px;
    }

    .node text {
      font-size: 14px;
    }

    path.link {
      fill: none;
      stroke: #ccc;
      stroke-width: 1.5px;
    }

  </style>
</head>
<body>
  <div id="body">
    <div class="hint-top">click on a node to see details</div>
    <pre class="details" style="display:none"></pre>
    <div id="header">
      GBIF Deployment
      <div class="hint">click or option-click to expand or collapse</div>
    </div>
  </div>
  <script type="text/javascript">
    var m = [20, 120, 20, 120],
      w = 1280 - m[1] - m[3],
      h = 800 - m[0] - m[2],
      i = 0,
      root;

    var tree = d3.layout.tree()
      .size([h, w]);

    var diagonal = d3.svg.diagonal()
      .projection(function(d) { return [d.y, d.x]; });

    var vis = d3.select("#body").append("svg:svg")
      .attr("width", w + m[1] + m[3])
      .attr("height", h + m[0] + m[2])
      .append("svg:g")
      .attr("transform", "translate(" + m[3] + "," + m[0] + ")");


    root = JSON.parse('${it.data}');
    root.x0 = h / 2;
    root.y0 = 0;

    function toggleAll(d) {
      if (d.children) {
        d.children.forEach(toggleAll);
        toggle(d);
      }
    }

    function ellipsis(text,maxLength) {
      if (text.length >= maxLength){
        return text.substring(0,maxLength) + '...';
      }
      return text;
    }

    update(root);


    function update(source) {
      var duration = d3.event && d3.event.altKey ? 5000 : 500;

      // Compute the new tree layout.
      var nodes = tree.nodes(root).reverse();

      // Normalize for fixed-depth.
      nodes.forEach(function(d) { d.y = d.depth * 180; });

      // Update the nodes…
      var node = vis.selectAll("g.node")
        .data(nodes, function(d) { return d.id || (d.id = ++i); });

      // Enter any new nodes at the parent's previous position.
      var nodeEnter = node.enter().append("svg:g")
        .attr("class", "node")
        .attr("transform", function(d) { return "translate(" + source.y0 + "," + source.x0 + ")"; })
        .on("click", function(d) { toggle(d); update(d);
          var g = d3.select(this); // The node
          var div = d3.select("pre.details")
            .style("display","block")
            .html(JSON.stringify(d.data,null,2));});

      nodeEnter.append("svg:circle")
        .attr("r", 1e-6)
        .style("fill", function(d) { return d.available ? "lightsteelblue" : "red"; });

      nodeEnter.append("svg:text")
        .attr("x", function(d) { return d.children || d._children ? -10 : 10; })
        .attr("dy", ".35em")
        .attr("text-anchor", function(d) { return d.children || d._children ? "end" : "start"; })
        .text(function(d) { return d.type == 'SERVICE_INSTANCE' ? ellipsis(d.name,10) : d.name; })
        .style("fill-opacity", 1e-6);

      // Transition nodes to their new position.
      var nodeUpdate = node.transition()
        .duration(duration)
        .attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; });

      nodeUpdate.select("circle")
        .attr("r", 7.5)
        .style("fill", function(d) { return d.available ? "lightsteelblue" : "red"; });

      nodeUpdate.select("text")
        .style("fill-opacity", 1);

      // Transition exiting nodes to the parent's new position.
      var nodeExit = node.exit().transition()
        .duration(duration)
        .attr("transform", function(d) { return "translate(" + source.y + "," + source.x + ")"; })
        .remove();

      nodeExit.select("circle")
        .attr("r", 1e-6);

      nodeExit.select("text")
        .style("fill-opacity", 1e-6);

      // Update the links…
      var link = vis.selectAll("path.link")
        .data(tree.links(nodes), function(d) { return d.target.id; });

      // Enter any new links at the parent's previous position.
      link.enter().insert("svg:path", "g")
        .attr("class", "link")
        .attr("d", function(d) {
          var o = {x: source.x0, y: source.y0};
          return diagonal({source: o, target: o});
        })
        .transition()
        .duration(duration)
        .attr("d", diagonal);

      // Transition links to their new position.
      link.transition()
        .duration(duration)
        .attr("d", diagonal);

      // Transition exiting nodes to the parent's new position.
      link.exit().transition()
        .duration(duration)
        .attr("d", function(d) {
          var o = {x: source.x, y: source.y};
          return diagonal({source: o, target: o});
        })
        .remove();

      // Stash the old positions for transition.
      nodes.forEach(function(d) {
        d.x0 = d.x;
        d.y0 = d.y;
      });
    }

    // Toggle children.
    function toggle(d) {
      if (d.children) {
        d._children = d.children;
        d.children = null;
      } else {
        d.children = d._children;
        d._children = null;
      }
    }

  </script>
</body>
</html>
