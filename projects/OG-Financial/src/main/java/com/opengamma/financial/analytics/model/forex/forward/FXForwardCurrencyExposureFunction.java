/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.forward;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.forex.calculator.CurrencyExposureForexCalculator;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.discounting.DiscountingCurrencyExposureFunction;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the currency exposure for a FX forward
 * @deprecated Use {@link DiscountingCurrencyExposureFunction}
 */
@Deprecated
public class FXForwardCurrencyExposureFunction extends FXForwardMultiValuedFunction {
  private static final CurrencyExposureForexCalculator CALCULATOR = CurrencyExposureForexCalculator.getInstance();

  public FXForwardCurrencyExposureFunction() {
    super(ValueRequirementNames.FX_CURRENCY_EXPOSURE);
  }

  @Override
  protected Set<ComputedValue> getResult(final Forex fxForward, final YieldCurveBundle data, final ComputationTarget target, final Set<ValueRequirement> desiredValues,
      final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext) {
    final MultipleCurrencyAmount result = fxForward.accept(CALCULATOR, data);
    return Collections.singleton(new ComputedValue(spec, result));
  }

}
