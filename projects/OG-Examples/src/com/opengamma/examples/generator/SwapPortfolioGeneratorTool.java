/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.generator;

import com.opengamma.financial.generator.AbstractSwapPortfolioGeneratorTool;

/**
 * Utility for constructing a random swap portfolio.
 */
public class SwapPortfolioGeneratorTool extends AbstractSwapPortfolioGeneratorTool {

  @Override
  protected SwapSecurityGenerator createSwapSecurityGenerator() {
    return new SwapSecurityGenerator();
  }

}
