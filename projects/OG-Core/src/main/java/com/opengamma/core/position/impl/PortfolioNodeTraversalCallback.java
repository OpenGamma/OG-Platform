/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;

/**
 * Callback interface allowing the tree of portfolio nodes to be traversed.
 * <p>
 * This interface is used during tree traversal to receive events from each node and position.
 * See {@link AbstractPortfolioNodeTraversalCallback} for a convenient adaptor class.
 */
public interface PortfolioNodeTraversalCallback {

  /**
   * Event called before a node is traversed.
   * 
   * @param portfolioNode  the node to be traversed, not null
   */
  void preOrderOperation(PortfolioNode portfolioNode);

  /**
   * Event called after a node is traversed.
   * 
   * @param portfolioNode  the node that was traversed, not null
   */
  void postOrderOperation(PortfolioNode portfolioNode);

  /**
   * Event called before a position is traversed.
   * 
   * @param position  the position to be traversed, not null
   */
  void preOrderOperation(Position position);

  /**
   * Event called after a position is traversed.
   * 
   * @param position  the position that was traversed, not null
   */
  void postOrderOperation(Position position);

}
