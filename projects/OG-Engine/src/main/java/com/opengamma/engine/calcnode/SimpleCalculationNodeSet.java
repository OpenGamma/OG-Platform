/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.util.ArgumentChecker;

/**
 * Creates a set of more-or-less identical nodes, e.g. one for each core or a fixed number.
 */
public class SimpleCalculationNodeSet extends AbstractCollection<SimpleCalculationNode> implements InitializingBean {

  private SimpleCalculationNodeFactory _factory;
  private int _nodeCount;
  private double _nodesPerCore;
  private Collection<SimpleCalculationNode> _nodes;

  public SimpleCalculationNodeFactory getNodeFactory() {
    return _factory;
  }

  public void setNodeFactory(final SimpleCalculationNodeFactory factory) {
    ArgumentChecker.notNull(factory, "factory");
    _factory = factory;
  }

  public void setNodeCount(final int nodeCount) {
    ArgumentChecker.notNegative(nodeCount, "nodeCount");
    _nodeCount = nodeCount;
  }

  public int getNodeCount() {
    return _nodeCount;
  }

  public void setNodesPerCore(final double nodesPerCore) {
    ArgumentChecker.notNegativeOrZero(nodesPerCore, "nodesPerCore");
    _nodesPerCore = nodesPerCore;
  }

  public double getNodesPerCore() {
    return _nodesPerCore;
  }

  protected int getCores() {
    return Runtime.getRuntime().availableProcessors();
  }

  @Override
  public Iterator<SimpleCalculationNode> iterator() {
    return _nodes.iterator();
  }

  @Override
  public int size() {
    return _nodes.size();
  }

  @Override
  public void afterPropertiesSet() {
    ArgumentChecker.notNullInjected(getNodeFactory(), "nodeFactory");
    final int nodes;
    if (getNodeCount() == 0) {
      if (getNodesPerCore() == 0) {
        throw new IllegalStateException("Either nodeCount or nodesPerCore must be set");
      }
      nodes = (int) Math.ceil(getNodesPerCore() * (double) getCores());
    } else {
      nodes = getNodeCount();
    }
    _nodes = new ArrayList<SimpleCalculationNode>(nodes);
    for (int i = 0; i < nodes; i++) {
      _nodes.add(getNodeFactory().createNode());
    }
  }

}
