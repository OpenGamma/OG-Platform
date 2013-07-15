/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;

/**
 * Represents a function which transforms portfolio nodes and positions.
 *
 * @param <T>  the type returned by the mapper function
 */
public interface PortfolioMapperFunction<T> {

  /**
   * Maps from a portfolio node to a value of the required type
   *
   * @param node  the portfolio node, not null
   * @return the transformed portfolio node
   */
  T apply(PortfolioNode node);

  /**
   * Maps from a position to a value of the required type
   *
   * @param parent the parent portfolio node, not null
   * @param position the position, not null
   * @return the transformed position
   */
  T apply(PortfolioNode parent, Position position);

}
