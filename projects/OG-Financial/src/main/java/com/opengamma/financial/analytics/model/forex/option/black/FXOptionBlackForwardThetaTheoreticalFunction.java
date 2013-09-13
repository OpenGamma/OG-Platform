/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.calculator.ForwardBlackThetaTheoreticalForexCalculator;
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
 * The function to compute the theta of Forex options in the Black model.
 * @deprecated The parent class is deprecated
 */
@Deprecated
public class FXOptionBlackForwardThetaTheoreticalFunction extends FXOptionBlackMultiValuedFunction {

  /**
   * 
   */
  public FXOptionBlackForwardThetaTheoreticalFunction() {
    super(ValueRequirementNames.THETA);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative forex, final ForexOptionDataBundle<?> data, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues, final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext) {
    if (data instanceof SmileDeltaTermStructureDataBundle) {
      final double result = forex.accept(ForwardBlackThetaTheoreticalForexCalculator.getInstance(), data);
      return Collections.singleton(new ComputedValue(spec, result));
    }
    throw new OpenGammaRuntimeException("Can only calculate theoretical theta for surfaces with smiles");
  }
}
