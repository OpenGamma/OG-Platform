/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import com.opengamma.financial.security.cash.CashSecurity;

/**
 * Utility for constructing a random cash portfolio.
 */
public class CashPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {

  protected CashSecurityGenerator createCashSecurityGenerator() {
    return new CashSecurityGenerator();
  }

  @Override
  public PortfolioNodeGenerator createPortfolioNodeGenerator(final int size) {
    final CashSecurityGenerator securities = createCashSecurityGenerator();
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<CashSecurity>(securities, getSecurityPersister());
    return new LeafPortfolioNodeGenerator(new StaticNameGenerator("Cash"), positions, size);
  }

}
