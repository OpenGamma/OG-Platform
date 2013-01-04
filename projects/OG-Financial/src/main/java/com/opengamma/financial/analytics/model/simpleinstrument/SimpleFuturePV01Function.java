/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.simpleinstrument;

import com.opengamma.analytics.financial.commodity.derivative.SimpleFutureConverter;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
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
  protected Double computeValues(InstrumentDerivative derivative, SimpleFutureDataBundle market) {
    SimpleFuture simpleFuture = derivative.accept(SimpleFutureConverter.getInstance());
    return simpleFuture.getSettlement() * market.getMarketPrice() / 10000.0;
  }

}
