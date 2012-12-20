/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * Recursively loads all positions under a particular {@link PortfolioNode}.
 */
@PublicAPI
public class PositionAccumulator {

  /**
   * The set of positions.
   */
  private final Set<Position> _positions = new HashSet<Position>();

  /**
   * Gets all the positions beneath the starting node.
   * 
   * @param startNode  the starting node, not null
   * @return All positions accumulated during execution
   */
  public static Set<Position> getAccumulatedPositions(PortfolioNode startNode) {
    return new PositionAccumulator(startNode).getPositions();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an accumulator starting from the specified node.
   * 
   * @param startNode  the starting node, not null
   */
  public PositionAccumulator(PortfolioNode startNode) {
    ArgumentChecker.notNull(startNode, "Portfolio Node");
    PortfolioNodeTraverser.depthFirst(new Callback()).traverse(startNode);
  }

  /**
   * Gets the positions that were found.
   * 
   * @return the positions, not null
   */
  public Set<Position> getPositions() {
    return Collections.unmodifiableSet(_positions);
  }

  /**
   * Callback to match the positions.
   */
  private class Callback extends AbstractPortfolioNodeTraversalCallback {
    @Override
    public void preOrderOperation(Position position) {
      _positions.add(position);
    }
  }

}
