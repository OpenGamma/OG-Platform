/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;

/**
 * A breadth-first traversal of portfolio nodes
 */
public class BreadthFirstPortfolioNodeTraverser extends PortfolioNodeTraverser {

  /**
   * Creates a traverser.
   * 
   * @param callback the callback to invoke, not null
   */
  public BreadthFirstPortfolioNodeTraverser(final PortfolioNodeTraversalCallback callback) {
    super(callback);
    // PLAT-1431]
    throw new UnsupportedOperationException("[PLAT-1431] - breadth first is not correctly implemented");
  }

  /**
   * Traverse the nodes notifying using the callback.
   * 
   * @param portfolioNode the node to start from, null does nothing
   */
  @Override
  public void traverse(final PortfolioNode portfolioNode) {
    if (portfolioNode == null) {
      return;
    }
    traverse(portfolioNode, true);
  }

  /**
   * Traverse the nodes.
   * 
   * @param portfolioNode the node to start from, not null
   * @param firstPass true if first pass
   */
  protected void traverse(PortfolioNode portfolioNode, boolean firstPass) {
    if (firstPass) {
      getCallback().preOrderOperation(portfolioNode);
      for (Position position : portfolioNode.getPositions()) {
        getCallback().preOrderOperation(portfolioNode, position);
      }
    }
    if (!firstPass) {
      for (PortfolioNode subNode : portfolioNode.getChildNodes()) {
        traverse(subNode, true);
      }
      for (PortfolioNode subNode : portfolioNode.getChildNodes()) {
        traverse(subNode, false);
      }
    }
    if (firstPass) {
      for (Position position : portfolioNode.getPositions()) {
        getCallback().postOrderOperation(portfolioNode, position);
      }
      getCallback().postOrderOperation(portfolioNode);
    }
  }

}
