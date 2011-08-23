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
 * Abstract filtering function that supplies a simple implementation for all methods.   
 */
public abstract class AbstractFilteringFunction implements FilteringFunction {

  /**
   * The name of the function.
   */
  private final String _name;

  /**
   * Creates a new instance.
   * 
   * @param name  the descriptive name of the function, not null
   */
  protected AbstractFilteringFunction(final String name) {
    ArgumentChecker.notNull(name, "name");
    _name = name;
  }

  //-------------------------------------------------------------------------
  @Override
  public final String getName() {
    return _name;
  }

  //-------------------------------------------------------------------------
  /**
   * Position filter that accepts all positions.
   * 
   * @param position  the position to filter, not null
   * @return true always
   */
  @Override
  public boolean acceptPosition(final Position position) {
    return true;
  }

  /**
   * Portfolio node filter that accepts any non-empty nodes.
   * 
   * @param portfolioNode  the node to filter, not null
   * @return true if the node contains at least one node or position
   */
  @Override
  public boolean acceptPortfolioNode(final PortfolioNode portfolioNode) {
    return !portfolioNode.getChildNodes().isEmpty() || !portfolioNode.getPositions().isEmpty();
  }

}
