/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import com.opengamma.financial.security.equity.EquitySecurity;

/**
 * Utility for constructing a random equity portfolio.
 */
public class EquityPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {

  private static final int POSITION_GRANULARITY = 10;
  private static final int POSITION_RANGE = 100;

  @Override
  public PortfolioNodeGenerator createPortfolioNodeGenerator(final int size) {
    final EquitySecurityGenerator securities = new EquitySecurityGenerator();
    configure(securities);
    final PositionGenerator positions = new SimplePositionGenerator<EquitySecurity>(new RandomQuantityGenerator(POSITION_GRANULARITY, POSITION_RANGE), securities, getSecurityPersister());
    return new LeafPortfolioNodeGenerator(new StaticNameGenerator("Equity"), positions, size);
  }

}
