/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.generator;

import com.opengamma.financial.generator.AbstractCapFloorPortfolioGeneratorTool;
import com.opengamma.financial.generator.AbstractCapFloorSecurityGenerator;

/**
 * Utility for constructing a random Cap/Floor portfolio.
 */
public class CapFloorPortfolioGeneratorTool extends AbstractCapFloorPortfolioGeneratorTool {

  @Override
  protected AbstractCapFloorSecurityGenerator createCapFloorSecurityGenerator() {
    return new CapFloorSecurityGenerator();
  }

}
