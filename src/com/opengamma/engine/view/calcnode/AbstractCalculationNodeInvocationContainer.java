/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.Collection;
import java.util.Queue;

import com.opengamma.util.ArgumentChecker;

/**
 * Base class for objects that manage a set of AbstractCalculationNodes with the intention of
 * invoking job executions on them.
 * 
 * @param <T>  the type of the calculation node queue
 */
public abstract class AbstractCalculationNodeInvocationContainer<T extends Queue<AbstractCalculationNode>> {

  private final T _nodes;
  
  protected AbstractCalculationNodeInvocationContainer(final T nodes) {
    _nodes = nodes;
  }

  protected T getNodes() {
    return _nodes;
  }

  public void addNode(final AbstractCalculationNode node) {
    ArgumentChecker.notNull(node, "node");
    getNodes().add(node);
    onNodeChange();
  }

  public void addNodes(final Collection<AbstractCalculationNode> nodes) {
    ArgumentChecker.notNull(nodes, "nodes");
    getNodes().addAll(nodes);
    onNodeChange();
  }

  public void setNode(final AbstractCalculationNode node) {
    ArgumentChecker.notNull(node, "node");
    getNodes().clear();
    getNodes().add(node);
    onNodeChange();
  }

  public void setNodes(final Collection<AbstractCalculationNode> nodes) {
    ArgumentChecker.notNull(nodes, "nodes");
    getNodes().clear();
    getNodes().addAll(nodes);
    onNodeChange();
  }

  protected abstract void onNodeChange();

}
