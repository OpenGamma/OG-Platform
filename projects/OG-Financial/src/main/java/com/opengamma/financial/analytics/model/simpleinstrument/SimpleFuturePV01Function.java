/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.simpleinstrument;

import com.opengamma.analytics.financial.simpleinstruments.derivative.SimpleFuture;
import com.opengamma.analytics.financial.simpleinstruments.pricing.SimpleFutureDataBundle;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 *
 */
public class SimpleFuturePV01Function extends SimpleFutureFunction {

  public SimpleFuturePV01Function() {
    super(ValueRequirementNames.PV01);
  }

  @Override
  protected Double computeValues(SimpleFuture derivative, SimpleFutureDataBundle market) {
    return derivative.getSettlement() * market.getMarketPrice() / 10000.0;
  }

}
