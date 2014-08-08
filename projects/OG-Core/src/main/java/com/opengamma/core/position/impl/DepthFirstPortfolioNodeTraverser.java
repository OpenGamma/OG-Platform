/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;

/**
 * A depth-first traversal of the portfolio node tree
 */
public class DepthFirstPortfolioNodeTraverser extends PortfolioNodeTraverser {

  /**
   * Creates a traverser.
   * 
   * @param callback the callback to invoke, not null
   */
  public DepthFirstPortfolioNodeTraverser(final PortfolioNodeTraversalCallback callback) {
    super(callback);
  }

  /**
   * Traverse the nodes notifying using the callback.
   * 
   * @param portfolioNode the node to start from, null does nothing
   */
  @Override
  public void traverse(PortfolioNode portfolioNode) {
    if (portfolioNode == null) {
      return;
    }
    getCallback().preOrderOperation(portfolioNode);
    for (Position position : portfolioNode.getPositions()) {
      getCallback().preOrderOperation(portfolioNode, position);
    }
    for (PortfolioNode subNode : portfolioNode.getChildNodes()) {
      traverse(subNode);
    }
    for (Position position : portfolioNode.getPositions()) {
      getCallback().postOrderOperation(portfolioNode, position);
    }
    getCallback().postOrderOperation(portfolioNode);
  }

}
