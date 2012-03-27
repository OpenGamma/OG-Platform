/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.generator;

/**
 * Utility for constructing a random equity portfolio.
 */
public class EquityPortfolioGeneratorTool extends com.opengamma.financial.generator.EquityPortfolioGeneratorTool {

  @Override
  protected EquitySecurityGenerator createEquitySecurityGenerator() {
    return new EquitySecurityGenerator(this);
  }

}
