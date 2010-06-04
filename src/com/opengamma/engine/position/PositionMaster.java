/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.util.Set;

import com.opengamma.id.UniqueIdentifier;

/**
 * A master structure of all positions held by the organization.
 * <p>
 * The master is structured into a number of portfolios, each of which holds
 * positions in a flexible tree structure.
 */
public interface PositionMaster {

  /**
   * Gets the list of all portfolio identifiers.
   * @return the portfolio identifiers, unmodifiable, not null
   */
  Set<UniqueIdentifier> getPortfolioIds();

  /**
   * Finds a specific portfolio by identifier.
   * @param uid  the unique identifier, null returns null
   * @return the portfolio, null if not found
   * @throws IllegalArgumentException if the identifier is not from this position master
   */
  Portfolio getPortfolio(UniqueIdentifier uid);

  /**
   * Finds a specific node from any portfolio by identifier.
   * @param uid  the unique identifier, null returns null
   * @return the node, null if not found
   * @throws IllegalArgumentException if the identifier is not from this position master
   */
  PortfolioNode getPortfolioNode(UniqueIdentifier uid);

  /**
   * Finds a specific position from any portfolio by identifier.
   * @param uid  the unique identifier, null returns null
   * @return the position, null if not found
   * @throws IllegalArgumentException if the identifier is not from this position master
   */
  Position getPosition(UniqueIdentifier uid);

}
