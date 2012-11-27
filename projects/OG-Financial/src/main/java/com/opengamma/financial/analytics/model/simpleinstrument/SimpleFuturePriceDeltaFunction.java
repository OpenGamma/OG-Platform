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
 * A Future's sensitivity to moves in its own underlying price, plainly the unit amount.
 */
public class SimpleFuturePriceDeltaFunction extends SimpleFutureFunction {

  public SimpleFuturePriceDeltaFunction() {
    super(ValueRequirementNames.VALUE_DELTA);
  }

  protected Double computeValues(SimpleFuture derivative, SimpleFutureDataBundle market) {
    return derivative.getUnitAmount();
  }

  @Override
  protected Double computeValues(InstrumentDerivative derivative, SimpleFutureDataBundle market) {
    SimpleFuture simpleFuture = derivative.accept(SimpleFutureConverter.getInstance());
    return simpleFuture.getUnitAmount();
  }

}
