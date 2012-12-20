/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.simpleinstrument;

import com.opengamma.analytics.financial.commodity.derivative.CommodityFuture;
import com.opengamma.analytics.financial.simpleinstruments.derivative.SimpleFuture;
import com.opengamma.analytics.financial.simpleinstruments.pricing.SimpleFutureDataBundle;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Sensitivity to a unit (100%) move in the rates of the funding curve
 * This assumes a model of the form V(t) = futuresPrice - referencePrice,
 * where futuresPrice = spot_underlying(t) * exp(-incomeYield*(T-t)) * exp(R*(T-t)).
 */
public class SimpleFutureRhoFunction extends SimpleFutureFunction {

  public SimpleFutureRhoFunction() {
    super(ValueRequirementNames.VALUE_RHO);
  }

  protected Object computeValues(SimpleFuture derivative, SimpleFutureDataBundle market) {
    return derivative.getSettlement() * market.getMarketPrice();
  }

  @Override
  protected <T extends CommodityFuture> Object computeValues(T derivative, SimpleFutureDataBundle market) {
    return derivative.getSettlement() * market.getMarketPrice();
  }

}
