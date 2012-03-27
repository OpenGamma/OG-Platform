/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.generator;

import com.opengamma.financial.security.equity.EquitySecurity;

/**
 * Utility for constructing a random equity option portfolio.
 */
public class EquityOptionPortfolioGeneratorTool extends com.opengamma.financial.generator.EquityOptionPortfolioGeneratorTool {

  @Override
  protected EquityOptionSecurityGenerator createEquityOptionSecurityGenerator(final EquitySecurity underlying) {
    return new EquityOptionSecurityGenerator(this, underlying);
  }

}
