/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.forward.deprecated;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.calculator.PresentValueMCACalculator;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.forex.forward.FXForwardFXPresentValueFunction;
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * @deprecated Use the version that does not refer to funding or forward curves
 * @see FXForwardFXPresentValueFunction
 */
@Deprecated
public class FXForwardPresentValueFunctionDeprecated extends FXForwardMultiValuedFunctionDeprecated {
  private static final PresentValueMCACalculator CALCULATOR = PresentValueMCACalculator.getInstance();

  public FXForwardPresentValueFunctionDeprecated() {
    super(ValueRequirementNames.FX_PRESENT_VALUE);
  }

  @Override
  protected Set<ComputedValue> getResult(final Forex fxForward, final YieldCurveBundle data, final ValueSpecification spec) {
    final MultipleCurrencyAmount result = fxForward.accept(CALCULATOR, data);
    return Collections.singleton(new ComputedValue(spec, FXUtils.getMultipleCurrencyAmountAsMatrix(result)));
  }

}
