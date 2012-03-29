/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.forex.calculator.CurrencyExposureForexCalculator;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * 
 */
public class ForexForwardCurrencyExposureFunction extends ForexForwardFunction {
  private static final CurrencyExposureForexCalculator CALCULATOR = CurrencyExposureForexCalculator.getInstance();

  public ForexForwardCurrencyExposureFunction() {
    super(ValueRequirementNames.FX_CURRENCY_EXPOSURE);
  }

  @Override
  protected Set<ComputedValue> getResult(final Forex fxForward, final YieldCurveBundle data, final ValueSpecification spec) {
    final MultipleCurrencyAmount result = CALCULATOR.visit(fxForward, data);
    return Collections.singleton(new ComputedValue(spec, FXUtils.getMultipleCurrencyAmountAsMatrix(result)));
  }

}
