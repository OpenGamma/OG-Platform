/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.generator;

import com.opengamma.financial.generator.AbstractFRAPortfolioGeneratorTool;

/**
 * Utility for constructing a random FRA portfolio.
 */
public class FRAPortfolioGeneratorTool extends AbstractFRAPortfolioGeneratorTool {

  @Override
  protected FRASecurityGenerator createFRASecurityGenerator() {
    return new FRASecurityGenerator();
  }

}
