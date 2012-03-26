/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.generator;

import com.opengamma.financial.generator.AbstractEquityOptionPortfolioGeneratorTool;
import com.opengamma.financial.generator.AbstractEquityOptionSecurityGenerator;
import com.opengamma.financial.security.equity.EquitySecurity;

/**
 * Utility for constructing a random equity option portfolio.
 */
public class EquityOptionPortfolioGeneratorTool extends AbstractEquityOptionPortfolioGeneratorTool {

  @Override
  protected AbstractEquityOptionSecurityGenerator createEquityOptionSecurityGenerator(final EquitySecurity underlying) {
    return new EquityOptionSecurityGenerator(underlying);
  }

}
