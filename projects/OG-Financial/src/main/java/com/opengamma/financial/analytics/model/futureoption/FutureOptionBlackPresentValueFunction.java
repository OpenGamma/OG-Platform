/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.futureoption;

import com.opengamma.analytics.financial.commodity.derivative.CommodityFutureOption;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackCalculator;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 *
 */
public class FutureOptionBlackPresentValueFunction extends FutureOptionBlackFunction {

  // TODO: CREATE A GENERAL FUTURES_OPTIONS BLACK PRICER
  private static final PresentValueBlackCalculator CALC = PresentValueBlackCalculator.getInstance();

  public FutureOptionBlackPresentValueFunction() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  protected Object computeValues(CommodityFutureOption<?> derivative, StaticReplicationDataBundle market) {
    final double pv = 0; //CALC.visitCommodityFutureOption(derivative, market);
    return pv;
  }

}
