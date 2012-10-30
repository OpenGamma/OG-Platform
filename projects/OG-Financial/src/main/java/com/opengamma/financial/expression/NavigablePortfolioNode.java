/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.expression;

import java.util.List;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.id.UniqueId;

/**
 * Navigable form of {@link PortfolioNode} that holds a full reference to its
 * parent and depth count in the graph.
 */
public class NavigablePortfolioNode implements PortfolioNode {

  private final NavigablePortfolioNode _parent;
  private final int _depth;
  private final PortfolioNode _node;

  public NavigablePortfolioNode(final NavigablePortfolioNode parent, final PortfolioNode node) {
    assert parent.getUniqueId().equals(node.getParentNodeId());
    _parent = parent;
    _depth = parent._depth + 1;
    _node = node;
  }

  public NavigablePortfolioNode(final PortfolioNode node) {
    assert node.getParentNodeId() == null;
    _parent = null;
    _depth = 1;
    _node = node;
  }

  @Override
  public UniqueId getUniqueId() {
    return _node.getUniqueId();
  }

  @Override
  public UniqueId getParentNodeId() {
    return _node.getParentNodeId();
  }

  public NavigablePortfolioNode getNavigableParentNode() {
    return _parent;
  }

  @Override
  public int size() {
    return _node.size();
  }

  @Override
  public List<PortfolioNode> getChildNodes() {
    return _node.getChildNodes();
  }

  @Override
  public List<Position> getPositions() {
    return _node.getPositions();
  }

  @Override
  public String getName() {
    return _node.getName();
  }

  public int getDepth() {
    return _depth;
  }

}
