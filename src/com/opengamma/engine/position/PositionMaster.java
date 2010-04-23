/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.util.Collection;

import com.opengamma.id.Identifier;

/**
 * A master structure of all positions held by the organization.
 * <p>
 * The master will hold all the positions structured into arbitrary nodes
 * and business-level portfolios.
 *
 * @author kirk
 */
public interface PositionMaster {

  /**
   * Gets the names of all root portfolios.
   * @return the root portfolio names, modifiable, never null
   */
  Collection<String> getRootPortfolioNames();

  /**
   * Gets a specific root portfolio by name.
   * @param portfolioName  the name, null returns null
   * @return the portfolio, null if not found
   */
  Portfolio getRootPortfolio(String portfolioName);

  /**
   * Gets a specific portfolio by identifier.
   * @param identifier  the identifier, null returns null
   * @return the portfolio, null if not found
   */
  Position getPosition(Identifier identifier);

  /**
   * Gets a specific node by identifier.
   * @param identifier  the identifier, null returns null
   * @return the node, null if not found
   */
  PortfolioNode getPortfolioNode(Identifier identifier);

}
