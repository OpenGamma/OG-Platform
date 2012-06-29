/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.forward.deprecated;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.forex.calculator.CurrencyExposureForexCalculator;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.forex.forward.ForexForwardCurrencyExposureFunction;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * @deprecated Use the version that does not refer to funding or forward curves
 * @see ForexForwardCurrencyExposureFunction
 */
@Deprecated
public class ForexForwardCurrencyExposureFunctionDeprecated extends ForexForwardMultiValuedFunctionDeprecated {
  private static final CurrencyExposureForexCalculator CALCULATOR = CurrencyExposureForexCalculator.getInstance();

  public ForexForwardCurrencyExposureFunctionDeprecated() {
    super(ValueRequirementNames.FX_CURRENCY_EXPOSURE);
  }

  @Override
  protected Set<ComputedValue> getResult(final Forex fxForward, final YieldCurveBundle data, final ValueSpecification spec) {
    final MultipleCurrencyAmount result = CALCULATOR.visit(fxForward, data);
    return Collections.singleton(new ComputedValue(spec, result));
  }

}
