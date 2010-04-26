/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.util.ArgumentChecker;

/**
 * Recursively loads all positions under a particular {@link PortfolioNode}.
 *
 * @author kirk
 */
public class PositionAccumulator{
  private final Set<Position> _positions = new HashSet<Position>();
  
  public PositionAccumulator(PortfolioNode portfolioNode) {
    ArgumentChecker.notNull(portfolioNode, "Portfolio Node");
    new PortfolioNodeTraverser(new Callback()).traverse(portfolioNode);
  }
  
  /**
   * @return the positions
   */
  public Set<Position> getPositions() {
    return Collections.unmodifiableSet(_positions);
  }

  private class Callback extends AbstractPortfolioNodeTraversalCallback {
    @Override
    public void preOrderOperation(Position position) {
      _positions.add(position);
    }
  }
  
  public static Set<Position> getAccumulatedPositions(PortfolioNode portfolioNode) {
    return new PositionAccumulator(portfolioNode).getPositions();
  }

}
