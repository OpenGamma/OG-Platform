/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml;

import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;

public class PortfolioPosition {

  private final ManageablePosition position;

  private final ManageableSecurity[] securities;

  private final String[] portfolioPath;

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
