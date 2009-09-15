/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.analytics.LiveDataSourcingFunction;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.SecurityDependencyGraph;
import com.opengamma.engine.security.Security;

/**
 * The physical plan corresponding to a particular security type logical plan.
 *
 * @author kirk
 */
public class PerSecurityExecutionPlan {
  private static final Logger s_logger = LoggerFactory.getLogger(PerSecurityExecutionPlan.class);
  private final Security _security;
  private final SecurityDependencyGraph _securityTypeGraph;
  private final List<DependencyNode> _orderedNodes = new ArrayList<DependencyNode>(); 
  
  public PerSecurityExecutionPlan(
      Security security,
      SecurityDependencyGraph securityTypeGraph) {
    if(security == null) {
      throw new NullPointerException("Security must be specified.");
    }
    if(securityTypeGraph == null) {
      throw new NullPointerException("Logical security type dependency graph must be specified.");
    }
    if(!ObjectUtils.equals(security, securityTypeGraph.getSecurity())) {
      throw new IllegalArgumentException("Security provided doesn't match security type graph.");
    }
    _security = security;
    _securityTypeGraph = securityTypeGraph;
  }

  /**
   * @return the security
   */
  public Security getSecurity() {
    return _security;
  }

  /**
   * @return the logicalSecurityTypeGraph
   */
  public SecurityDependencyGraph getLogicalSecurityTypeGraph() {
    return _securityTypeGraph;
  }
  
  /**
   * @return the orderedNodes
   */
  public List<DependencyNode> getOrderedNodes() {
    return Collections.unmodifiableList(_orderedNodes);
  }

  public void buildExecutionPlan() {
    // Done this way for performance rather than a linear search through _orderedNodes.
    Set<DependencyNode> includedNodes = new HashSet<DependencyNode>();
    for(DependencyNode topLevelNode : getLogicalSecurityTypeGraph().getTopLevelNodes()) {
      buildExecutionPlan(topLevelNode, includedNodes);
    }
    assert _orderedNodes.size() == includedNodes.size();
    s_logger.info("Built execution plan for {} with {} elements", getSecurity(), _orderedNodes.size());
  }

  /**
   * DFS search, post-order execution.
   * 
   * @param topLevelNode
   * @param includedNodes
   */
  protected void buildExecutionPlan(
      DependencyNode node,
      Set<DependencyNode> includedNodes) {
    if(includedNodes.contains(node)) {
      return;
    }
    for(DependencyNode childNode : node.getInputNodes()) {
      buildExecutionPlan(childNode, includedNodes);
    }
    assert !includedNodes.contains(node) : "Cycle detected.";
    // Deal with the special live data sourcing functions: we already populate the cache
    // with these.
    if(!(node.getFunction() instanceof LiveDataSourcingFunction)) {
      _orderedNodes.add(node);
      includedNodes.add(node);
    }
  }

}
