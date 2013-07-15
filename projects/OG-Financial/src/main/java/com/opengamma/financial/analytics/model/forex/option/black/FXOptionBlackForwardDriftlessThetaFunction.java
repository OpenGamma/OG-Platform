/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.calculator.ForwardBlackDriftlessThetaForexCalculator;
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

/**
 * The function to compute the forward driftless theta of Forex options in the Black model.
 */
public class FXOptionBlackForwardDriftlessThetaFunction extends FXOptionBlackMultiValuedFunction {

  public FXOptionBlackForwardDriftlessThetaFunction() {
    super(ValueRequirementNames.FORWARD_DRIFTLESS_THETA);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative forex, final ForexOptionDataBundle<?> data, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues, final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext) {
    if (data instanceof SmileDeltaTermStructureDataBundle) {
      final double result = forex.accept(ForwardBlackDriftlessThetaForexCalculator.getInstance(), data);
      return Collections.singleton(new ComputedValue(spec, result));
    }
    throw new OpenGammaRuntimeException("Can only calculate forward driftless theta for surfaces with smiles");
  }
}
