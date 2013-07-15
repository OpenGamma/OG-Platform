/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.position;

import java.util.Collection;
import java.util.Map;

import com.opengamma.util.PublicSPI;

/**
 * A position in a security held in a portfolio.
 * <p>
 * A position is fundamentally a quantity of a security.
 * For example, a position might be 50 shares of OpenGamma.
 * <p>
 * It differs from a trade in that it may represent the combined result from a set of trades.
 * The collection of trades is optional and if present may be incomplete.
 * <p>
 * This interface is read-only.
 * Implementations may be mutable.
 */
@PublicSPI
public interface Position extends PositionOrTrade {

  /**
   * Gets the trades that are immediate children of this position.
   * <p>
   * The set of trades is optional and if present may be incomplete.
   * 
   * @return the trades, unmodifiable, not null
   */
  Collection<Trade> getTrades();
  
  /**
   * Gets the attributes to use for position aggregation.
   * 
   * @return the attributes, not null
   */
  Map<String, String> getAttributes();

}
