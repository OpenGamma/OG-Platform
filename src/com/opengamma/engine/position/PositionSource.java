/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.util.Set;

import com.opengamma.id.UniqueIdentifier;

/**
 * A source of portfolios and positions as accessed by the engine.
 * <p>
 * This interface provides a simple view of portfolios and positions as needed by the engine.
 * This may be backed by a full-featured position master, or by a much simpler data structure.
 */
public interface PositionSource {

  /**
   * Gets the list of all portfolio identifiers.
   * @return the portfolio identifiers, unmodifiable, not null
   */
  Set<UniqueIdentifier> getPortfolioIds();

  /**
   * Finds a specific portfolio by identifier.
   * @param uid  the unique identifier, null returns null
   * @return the portfolio, null if not found
   * @throws IllegalArgumentException if the identifier is invalid
   */
  Portfolio getPortfolio(UniqueIdentifier uid);

  /**
   * Finds a specific node from any portfolio by identifier.
   * @param uid  the unique identifier, null returns null
   * @return the node, null if not found
   * @throws IllegalArgumentException if the identifier is invalid
   */
  PortfolioNode getPortfolioNode(UniqueIdentifier uid);

  /**
   * Finds a specific position from any portfolio by identifier.
   * @param uid  the unique identifier, null returns null
   * @return the position, null if not found
   * @throws IllegalArgumentException if the identifier is invalid
   */
  Position getPosition(UniqueIdentifier uid);

}
