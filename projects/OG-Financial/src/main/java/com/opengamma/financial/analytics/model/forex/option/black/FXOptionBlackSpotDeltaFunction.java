/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.calculator.SpotBlackDeltaForexCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.ForexOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.black.BlackDiscountingSpotDeltaFXOptionFunction;

/**
 * The function to compute the spot delta of Forex options in the Black model.
 * @deprecated Use {@link BlackDiscountingSpotDeltaFXOptionFunction}
 */
@Deprecated
public class FXOptionBlackSpotDeltaFunction extends FXOptionBlackMultiValuedFunction {

  public FXOptionBlackSpotDeltaFunction() {
    super(ValueRequirementNames.DELTA);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative forex, final ForexOptionDataBundle<?> data, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues, final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext) {
    if (data instanceof SmileDeltaTermStructureDataBundle) {
      final double result = forex.accept(SpotBlackDeltaForexCalculator.getInstance(), data);
      return Collections.singleton(new ComputedValue(spec, result));
    }
    throw new OpenGammaRuntimeException("Can only calculate delta for surfaces with smiles");
  }

}
