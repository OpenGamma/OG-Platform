/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml;

import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;

/**
 * Represents an individual position in a portfolio, potentially nested within
 * a sub-portfolio.
 */
public class PortfolioPosition {

  /**
   * The manageable position in the portfolio, not null.
   */
  private final ManageablePosition position;

  /**
   * The securities used for this position, not null.
   */
  private final ManageableSecurity[] securities;

  /**
   * The path to this position in the portfolio, not null.
   */
  private final String[] portfolioPath;

  /**
   * Construct the portfolio position.
   *
   * @param position the position, not null
   * @param securities the security, not null
   * @param portfolioPath the portfolio path, not null
   */
  public PortfolioPosition(ManageablePosition position, ManageableSecurity[] securities, String[] portfolioPath) {
    this.position = position;
    this.securities = securities;
    this.portfolioPath = portfolioPath;
  }

  public ManageablePosition getPosition() {
    return position;
  }

  public ManageableSecurity[] getSecurities() {
    return securities;
  }

  public String[] getPortfolioPath() {
    return portfolioPath;
  }
}
