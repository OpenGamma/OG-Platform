/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.forward;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.calculator.PresentValueMCACalculator;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.discounting.DiscountingFXPVFunction;
import com.opengamma.financial.analytics.model.forex.FXUtils;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the FX present value for FX forwards.
 * @deprecated Use {@link DiscountingFXPVFunction}
 */
@Deprecated
public class FXForwardFXPresentValueFunction extends FXForwardMultiValuedFunction {
  /** The present value calculator */
  private static final PresentValueMCACalculator CALCULATOR = PresentValueMCACalculator.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#FX_PRESENT_VALUE}
   */
  public FXForwardFXPresentValueFunction() {
    super(ValueRequirementNames.FX_PRESENT_VALUE);
  }

  @Override
  protected Set<ComputedValue> getResult(final Forex fxForward, final YieldCurveBundle data, final ComputationTarget target, final Set<ValueRequirement> desiredValues,
      final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext) {
    final MultipleCurrencyAmount result = fxForward.accept(CALCULATOR, data);
    return Collections.singleton(new ComputedValue(spec, FXUtils.getMultipleCurrencyAmountAsMatrix(result)));
  }

}
