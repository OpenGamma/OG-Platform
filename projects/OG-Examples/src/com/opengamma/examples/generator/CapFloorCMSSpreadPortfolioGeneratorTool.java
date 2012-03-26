/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.generator;

import com.opengamma.financial.generator.AbstractCapFloorCMSSpreadPortfolioGeneratorTool;
import com.opengamma.financial.generator.AbstractCapFloorCMSSpreadSecurityGenerator;

/**
 * Utility for constructing a random Cap/Floor CMS spread portfolio.
 */
public class CapFloorCMSSpreadPortfolioGeneratorTool extends AbstractCapFloorCMSSpreadPortfolioGeneratorTool {

  @Override
  protected AbstractCapFloorCMSSpreadSecurityGenerator createCapFloorCMSSpreadSecurityGenerator() {
    return new CapFloorCMSSpreadSecurityGenerator();
  }

}
