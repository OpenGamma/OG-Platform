/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

/**
 * A portfolio of positions, typically having business-level meaning.
 * <p>
 * A portfolio is the primary element of business-level grouping within the position master.
 * It consists of a number of positions which are grouped using nodes.
 * <p>
 * A portfolio typically has meta-data.
 *
 * @author kirk
 */
public interface Portfolio extends PortfolioNode {

  /**
   * Gets the name of the portfolio.
   * @return the name
   */
  String getPortfolioName();

}
