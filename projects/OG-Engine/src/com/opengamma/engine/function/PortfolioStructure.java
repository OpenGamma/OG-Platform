/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionSource;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * Service to interrogate the portfolio structure 
 */
public class PortfolioStructure {

  private final PositionSource _positionSource;

  public PortfolioStructure(final PositionSource positionSource) {
    ArgumentChecker.notNull(positionSource, "positionSource");
    _positionSource = positionSource;
  }

  public PositionSource getPositionSource() {
    return _positionSource;
  }

  private PortfolioNode getParentNodeImpl(final PortfolioNode node) {
    final UniqueIdentifier parent = node.getParentNode();
    if (parent != null) {
      return getPositionSource().getPortfolioNode(parent);
    } else {
      return null;
    }
  }

  public PortfolioNode getParentNode(final PortfolioNode node) {
    ArgumentChecker.notNull(node, "node");
    return getParentNodeImpl(node);
  }

  public PortfolioNode getParentNode(final Position position) {
    ArgumentChecker.notNull(position, "position");
    final UniqueIdentifier parent = position.getPortfolioNode();
    if (parent != null) {
      return getPositionSource().getPortfolioNode(parent);
    } else {
      return null;
    }
  }

  private PortfolioNode getRootPortfolioNodeImpl(PortfolioNode node) {
    UniqueIdentifier parent = node.getParentNode();
    while (parent != null) {
      node = getPositionSource().getPortfolioNode(parent);
      if (node == null) {
        // Position source is broken
        return null;
      }
      parent = node.getParentNode();
    }
    return node;
  }

  public PortfolioNode getRootPortfolioNode(final PortfolioNode node) {
    ArgumentChecker.notNull(node, "node");
    return getRootPortfolioNodeImpl(node);
  }

  public PortfolioNode getRootPortfolioNode(final Position position) {
    final PortfolioNode node = getParentNode(position);
    if (node != null) {
      return getRootPortfolioNodeImpl(node);
    } else {
      return null;
    }
  }

  private void getAllPositionsImpl(final PortfolioNode node, final List<Position> result) {
    result.addAll(node.getPositions());
    for (PortfolioNode child : node.getChildNodes()) {
      getAllPositionsImpl(child, result);
    }
  }

  public List<Position> getAllPositions(final PortfolioNode node) {
    ArgumentChecker.notNull(node, "node");
    final List<Position> result = new ArrayList<Position>();
    getAllPositionsImpl(node, result);
    return result;
  }

}
