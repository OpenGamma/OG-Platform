/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import com.opengamma.analytics.financial.equity.EquityOptionDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionPresentValueCalculator;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 *
 */
public class EquityIndexOptionPresentValueFunction extends EquityIndexOptionFunction {
  private static final EquityIndexOptionPresentValueCalculator s_calculator = EquityIndexOptionPresentValueCalculator.getInstance();

  public EquityIndexOptionPresentValueFunction() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  protected Object computeValues(EquityIndexOption derivative, EquityOptionDataBundle market) {
    final double pv = s_calculator.visitEquityIndexOption(derivative, market);
    return pv;
  }

}
