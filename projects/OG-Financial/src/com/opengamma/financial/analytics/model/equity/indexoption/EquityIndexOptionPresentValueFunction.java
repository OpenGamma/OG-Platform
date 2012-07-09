/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.indexoption;

import com.opengamma.analytics.financial.equity.EquityOptionDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionPresentValueCalculator;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;

/**
 *
 */
public class EquityIndexOptionPresentValueFunction extends EquityIndexOptionFunction {
  private static final EquityIndexOptionPresentValueCalculator s_calculator = EquityIndexOptionPresentValueCalculator.getInstance();

  public EquityIndexOptionPresentValueFunction() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  protected Object computeValues(EquityIndexOption derivative, EquityOptionDataBundle market, Currency currency) {
    final double pv = s_calculator.visitEquityIndexOption(derivative, market);
    return CurrencyAmount.of(currency, pv);
  }

}
