/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.simpleinstrument;

import com.opengamma.analytics.financial.commodity.derivative.CommodityFuture;
import com.opengamma.analytics.financial.commodity.derivative.SimpleFutureConverter;
import com.opengamma.analytics.financial.commodity.pricing.CommodityFuturePresentValueCalculator;
import com.opengamma.analytics.financial.simpleinstruments.derivative.SimpleFuture;
import com.opengamma.analytics.financial.simpleinstruments.pricing.SimpleFutureDataBundle;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Simple Function computes PV as the difference between Live and last day's closing prices
 */
public class SimpleFuturePresentValueFunction extends SimpleFutureFunction {

  public SimpleFuturePresentValueFunction() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  private static final CommodityFuturePresentValueCalculator CALCULATOR = new CommodityFuturePresentValueCalculator();

  @Override
  protected <T extends CommodityFuture> Object computeValues(T derivative, SimpleFutureDataBundle market) {
    SimpleFuture simpleFuture = derivative.accept(SimpleFutureConverter.getInstance());
    final Double pv = simpleFuture.accept(CALCULATOR, market);
    return pv;
  }

}
