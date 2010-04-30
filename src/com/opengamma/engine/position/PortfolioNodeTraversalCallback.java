/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

/**
 * Callback interface allowing the position master to be traversed.
 */
public interface PortfolioNodeTraversalCallback {

  void preOrderOperation(PortfolioNode portfolioNode);

  void postOrderOperation(PortfolioNode portfolioNode);

  void preOrderOperation(Position position);

  void postOrderOperation(Position position);

}
