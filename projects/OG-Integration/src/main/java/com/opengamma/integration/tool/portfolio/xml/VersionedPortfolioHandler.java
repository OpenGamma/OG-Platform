/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml;

/**
 * The VersionedPortfolioHandler acts as a buffer between the version-specific
 * xml parsing code and the rest of the system allowing a standard interface
 * to be used across different versions of the xml schema. Note that
 * as the class holds an iterator, it is stateful and not thread safe.
 */
public class VersionedPortfolioHandler {

  /**
   * The portfolio positions, not null.
   */
  private Iterable<PortfolioPosition> _positions;

  /**
   * The portfolio name, may be null.
   */
  private String _portfolioName;

  /**
   * Create a handler for the portfolio.
   *
   * @param name the name of the portfolio
   * @param positions the positions to be handled
   */
  public VersionedPortfolioHandler(String name, Iterable<PortfolioPosition> positions) {
    _portfolioName = name;
    _positions = positions;
  }

  /**
   * Get the positions for the portfolio, not null.
   *
   * @return the positions
   */
  public Iterable<PortfolioPosition> getPositions() {
    return _positions;
  }

  /**
   * Get the name for the portfolio.
   *
   * @return the portfolio name, may be null
   */
  public String getPortfolioName() {
    return _portfolioName;
  }
}
