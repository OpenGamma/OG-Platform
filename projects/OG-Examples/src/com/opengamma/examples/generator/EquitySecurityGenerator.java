/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.generator;

import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Source of random, but reasonable, equity security instances.
 */
public class EquitySecurityGenerator extends com.opengamma.financial.generator.EquitySecurityGenerator {

  private final AbstractPortfolioGeneratorTool _tool;

  public EquitySecurityGenerator(final AbstractPortfolioGeneratorTool tool) {
    ArgumentChecker.notNull(tool, "tool");
    _tool = tool;
  }

  protected AbstractPortfolioGeneratorTool getTool() {
    return _tool;
  }

  @Override
  public EquitySecurity createSecurity() {
    final EquitySecurity security = super.createSecurity();
    if (security != null) {
      RandomHistoricalData.createPriceSeries(getTool(), security, getTool().getRandom().nextInt(400) + 20);
    }
    return security;
  }

}
