/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.filtering;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.util.ArgumentChecker;

/**
 * Partial implementation of {@link FilteringFunction} with default behaviour for all methods.   
 */
public abstract class AbstractFilteringFunction implements FilteringFunction {

  private final String _name;

  /**
   * Creates a new instance.
   * 
   * @param name the name to be returned by {@link #getName}, not {@code null}
   */
  protected AbstractFilteringFunction(final String name) {
    ArgumentChecker.notNull(name, "name");
    _name = name;
  }

  /**
   * Default position filter. All positions are accepted.
   * 
   * @param position ignored
   * @return always {@code true}
   */
  @Override
  public boolean acceptPosition(final Position position) {
    return true;
  }

  /**
   * Default portfolio node filter. Any non-empty portfolio nodes are accepted.
   * 
   * @param portfolioNode node to consider
   * @return {@code true} if the node contains at least one node or position, {@code false} otherwise
   */
  @Override
  public boolean acceptPortfolioNode(final PortfolioNode portfolioNode) {
    return !portfolioNode.getChildNodes().isEmpty() || !portfolioNode.getPositions().isEmpty();
  }

  @Override
  public final String getName() {
    return _name;
  }
}
