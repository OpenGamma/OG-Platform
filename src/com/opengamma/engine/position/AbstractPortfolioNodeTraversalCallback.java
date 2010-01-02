/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

/**
 * An implementation of {@link PortfolioNodeTraversalCallback} which has all callback
 * methods as no-ops. Subclasses should override the particular methods they require.
 *
 * @author kirk
 */
public class AbstractPortfolioNodeTraversalCallback
implements PortfolioNodeTraversalCallback {

  @Override
  public void postOrderOperation(PortfolioNode portfolioNode) {
  }

  @Override
  public void postOrderOperation(Position position) {
  }

  @Override
  public void preOrderOperation(PortfolioNode portfolioNode) {
  }

  @Override
  public void preOrderOperation(Position position) {
  }

}
