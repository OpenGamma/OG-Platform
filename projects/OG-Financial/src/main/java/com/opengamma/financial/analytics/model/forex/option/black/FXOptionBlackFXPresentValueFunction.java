/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.forex.calculator.PresentValueBlackSmileForexCalculator;
import com.opengamma.analytics.financial.forex.calculator.PresentValueBlackTermStructureForexCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.ForexOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackForexTermStructureBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.black.BlackDiscountingFXPVFXOptionFunction;
import com.opengamma.financial.analytics.model.forex.FXUtils;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the FX present value for FX options using the Black method.
 * @deprecated Use {@link BlackDiscountingFXPVFXOptionFunction}
 */
@Deprecated
public class FXOptionBlackFXPresentValueFunction extends FXOptionBlackMultiValuedFunction {
  private static final PresentValueBlackTermStructureForexCalculator FLAT_CALCULATOR = PresentValueBlackTermStructureForexCalculator.getInstance();
  private static final PresentValueBlackSmileForexCalculator SMILE_CALCULATOR = PresentValueBlackSmileForexCalculator.getInstance();

  public FXOptionBlackFXPresentValueFunction() {
    super(ValueRequirementNames.FX_PRESENT_VALUE);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative forex, final ForexOptionDataBundle<?> data, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues, final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext) {
    if (data instanceof YieldCurveWithBlackForexTermStructureBundle) {
      final MultipleCurrencyAmount result = forex.accept(FLAT_CALCULATOR, data);
      return Collections.singleton(new ComputedValue(spec, FXUtils.getMultipleCurrencyAmountAsMatrix(result)));
    }
    final MultipleCurrencyAmount result = forex.accept(SMILE_CALCULATOR, data);
    return Collections.singleton(new ComputedValue(spec, FXUtils.getMultipleCurrencyAmountAsMatrix(result)));
  }

}
