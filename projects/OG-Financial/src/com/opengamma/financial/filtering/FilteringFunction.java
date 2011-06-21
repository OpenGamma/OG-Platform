/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.filtering;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;

/**
 * Function to filter portfolio nodes and positions from a portfolio. The filtering applies from the
 * leaves of the portfolio to the root. For example a portfolio node considered is the node after
 * the filter has been applied to its elements.  
 */
public interface FilteringFunction {

  /**
   * Consider a position for inclusion into the resulting portfolio.
   * 
   * @param position the position to consider, not {@code null}.
   * @return {@code true} if the position should be included, {@code false} to omit it
   */
  boolean acceptPosition(Position position);

  /**
   * Consider a portfolio node for inclusion into the resulting portfolio.
   * 
   * @param portfolioNode the node to consider, not {@code null}. This is the node after position
   * filtering has been applied.
   * @return {@code true} if the node should be included, {@code false} to omit it
   */
  boolean acceptPortfolioNode(PortfolioNode portfolioNode);

  /**
   * Returns the name of the filter. Filter names are not unique and provided for descriptive
   * information only.
   * 
   * @return the name, not {@code null}
   */
  String getName();
}
