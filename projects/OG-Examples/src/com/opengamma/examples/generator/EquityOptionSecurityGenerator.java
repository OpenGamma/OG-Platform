/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.generator;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.generator.AbstractEquityOptionSecurityGenerator;
import com.opengamma.financial.security.equity.EquitySecurity;

/**
 * Source of random, but reasonable, equity option security instances.
 */
public class EquityOptionSecurityGenerator extends AbstractEquityOptionSecurityGenerator {

  public EquityOptionSecurityGenerator(final EquitySecurity underlying) {
    super(underlying);
  }

  @Override
  protected String getPriceSeriesDataField() {
    return MarketDataRequirementNames.MARKET_VALUE;
  }

}
