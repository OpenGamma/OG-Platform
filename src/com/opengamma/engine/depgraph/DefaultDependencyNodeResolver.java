/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.HashSet;
import java.util.Set;

import com.opengamma.engine.analytics.AnalyticValueDefinition;

/**
 * 
 *
 * @author kirk
 */
public class DefaultDependencyNodeResolver implements DependencyNodeResolver {
  private final Set<DependencyNode> _nodes = new HashSet<DependencyNode>();
  
  @Override
  public DependencyNode resolve(AnalyticValueDefinition<?> outputValue) {
    for(DependencyNode node : _nodes) {
      if(node.getOutputValues().contains(outputValue)) {
        return node;
      }
    }
    return null;
  }
  
  public void addSubGraph(DependencyNode node) {
    if(_nodes.contains(node)) {
      return;
    }
    _nodes.add(node);
    for(DependencyNode subNode : node.getInputNodes()) {
      addSubGraph(subNode);
    }
  }
  
  public int size() {
    return _nodes.size();
  }

}
