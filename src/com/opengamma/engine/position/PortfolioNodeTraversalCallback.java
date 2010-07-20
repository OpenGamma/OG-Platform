/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

/**
 * Callback interface allowing the tree of portfolio nodes to be traversed.
 */
public interface PortfolioNodeTraversalCallback {

  /**
   * Event called before a node is traversed.
   * @param portfolioNode  the node to be traversed, not null
   */
  void preOrderOperation(PortfolioNode portfolioNode);

  /**
   * Event called after a node is traversed.
   * @param portfolioNode  the node that was traversed, not null
   */
  void postOrderOperation(PortfolioNode portfolioNode);

  /**
   * Event called before a position is traversed.
   * @param position  the position to be traversed, not null
   */
  void preOrderOperation(Position position);

  /**
   * Event called after a position is traversed.
   * @param position  the position that was traversed, not null
   */
  void postOrderOperation(Position position);

}
