/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.security.Security;

/**
 * Extends {@link PortfolioNodeImpl} to have {@link FullyPopulatedPosition} nodes.
 *
 * @author kirk
 */
public class FullyPopulatedPortfolioNode extends PortfolioNodeImpl {
  private final List<FullyPopulatedPosition> _fullyPopulatedPositions =
    new ArrayList<FullyPopulatedPosition>();
  
  public FullyPopulatedPortfolioNode(String name) {
    super(name);
  }
  
  public FullyPopulatedPortfolioNode() {
    super();
  }

  public void addPosition(Position position, Security security) {
    addPosition(position);
    FullyPopulatedPosition fullyPopulatedPosition = new FullyPopulatedPosition(position, security);
    _fullyPopulatedPositions.add(fullyPopulatedPosition);
  }
  
  public Collection<FullyPopulatedPosition> getPopulatedPositions() {
    return Collections.unmodifiableList(_fullyPopulatedPositions);
  }

  @SuppressWarnings("unchecked")
  public Collection<FullyPopulatedPortfolioNode> getPopulatedSubNodes() {
    return (Collection)getSubNodes();
  }
  
  @Override
  public void addSubNode(PortfolioNode subNode) {
    if(!(subNode instanceof FullyPopulatedPortfolioNode)) {
      throw new IllegalArgumentException("Fully Populated Portfolio Node can only have itself as subnodes.");
    }
    super.addSubNode(subNode);
  }
  
}
