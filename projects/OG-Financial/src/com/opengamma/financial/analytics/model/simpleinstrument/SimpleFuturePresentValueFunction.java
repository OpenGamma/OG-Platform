/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.simpleinstrument;

import com.opengamma.analytics.financial.simpleinstruments.derivative.SimpleFuture;
import com.opengamma.analytics.financial.simpleinstruments.pricing.SimpleFutureDataBundle;
import com.opengamma.analytics.financial.simpleinstruments.pricing.SimpleFuturePresentValueCalculator;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Simple Function computes PV as the difference between Live and last day's closing prices
 */
public class SimpleFuturePresentValueFunction extends SimpleFutureFunction {

  public SimpleFuturePresentValueFunction() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  private static final SimpleFuturePresentValueCalculator CALCULATOR = new SimpleFuturePresentValueCalculator();

  @Override
  protected Double computeValues(SimpleFuture derivative, SimpleFutureDataBundle market) {
    final Double pv = derivative.accept(CALCULATOR, market);
    return pv;
  }

}
