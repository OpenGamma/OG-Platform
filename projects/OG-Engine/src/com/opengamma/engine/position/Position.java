/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.util.Collection;

import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.PublicSPI;

/**
 * A position held within a portfolio.
 * <p>
 * A position is a business-level structure representing a quantity of a security.
 * For example, a position might be 50 shares of OpenGamma.
 * 
 * An aggregate of {@link Trade trades} makes a position.
 *
 */
@PublicSPI
public interface Position extends TradeOrPosition, UniqueIdentifiable {
  /**
   * Gets the unique identifier of the node within the portfolio this position is immediately under.
   * @return the unique identifier
   */
  UniqueIdentifier getPortfolioNode();
  /**
   * Gets the trades for the position if available
   * @return the trades, not null
   */
  Collection<Trade> getTrades();
}
