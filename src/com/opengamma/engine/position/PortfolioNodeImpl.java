/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A simple implementation of {@link PortfolioNode}. 
 *
 * @author kirk
 */
public class PortfolioNodeImpl implements PortfolioNode, Serializable {
  private final List<Position> _positions = new ArrayList<Position>();
  private final List<PortfolioNode> _subNodes = new ArrayList<PortfolioNode>();
  private final String _name;
  
  public PortfolioNodeImpl() {
    _name = null;
  }
  
  public PortfolioNodeImpl(String name) {
    _name = name;
  }
  
  @Override
  public Collection<Position> getPositions() {
    return Collections.unmodifiableList(_positions);
  }

  @Override
  public Collection<PortfolioNode> getSubNodes() {
    return Collections.unmodifiableList(_subNodes);
  }
  
  public void addPosition(Position position) {
    if(position == null) {
      throw new NullPointerException("Must specify a position to add.");
    }
    _positions.add(position);
  }
  
  public void addSubNode(PortfolioNode subNode) {
    if(subNode == null) {
      throw new NullPointerException("Must specify a sub-node to add.");
    }
    _subNodes.add(subNode);
  }
  
  @Override
  public String getName() {
    return _name;
  }

}
