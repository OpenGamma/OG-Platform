/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.filtering;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;

/**
 * Function to filter portfolio nodes and positions from a portfolio.
 * <p>
 * The filtering applies from the leaves of the portfolio to the root.
 * For example a portfolio node considered is the node after the filter has been applied
 * to its constituent elements.
 */
public interface FilteringFunction {

  /**
   * Gets the name of the filter.
   * Filter names are not unique and provided for descriptive information only.
   * 
   * @return the name, not null
   */
  String getName();

  /**
   * Filters a position.
   * <p>
   * The resulting portfolio will only contain the position if this method returns true.
   * 
   * @param position  the position to filter, not null
   * @return true to include the position, false to omit it
   */
  boolean acceptPosition(Position position);

  /**
   * Filters a node.
   * <p>
   * The resulting portfolio will only contain the node if this method returns true.
   * 
   * @param portfolioNode  the node to consider where its contents have already been filtered, not null
   * @return true to include the node, false to omit it
   */
  boolean acceptPortfolioNode(PortfolioNode portfolioNode);

}
