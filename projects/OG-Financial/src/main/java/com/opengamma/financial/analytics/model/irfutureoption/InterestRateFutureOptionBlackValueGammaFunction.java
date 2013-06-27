/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.analytics.financial.riskfactor.ValueGammaCalculator;
import com.opengamma.analytics.financial.riskfactor.ValueGreekCalculator;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Calculates the "ValueGamma" ({@link ValueRequirementNames.VALUE_GAMMA}) of an InterestRateFutureOption taking
 * the Black "Gamma" ({@link ValueRequirementNames.GAMMA}) as required input. 
 * The underlying Futures price is computed from the futures curve.
 */
public class InterestRateFutureOptionBlackValueGammaFunction extends InterestRateFutureOptionBlackFunction {
  
  /** Value gamma calculator */
  private static final ValueGreekCalculator CALCULATOR = ValueGammaCalculator.getInstance();
  
  public InterestRateFutureOptionBlackValueGammaFunction() {
    super(ValueRequirementNames.VALUE_GAMMA);
  }

  @Override
  protected Set<ComputedValue> getResult(InstrumentDerivative irFutureOption, YieldCurveWithBlackCubeBundle data, ValueSpecification spec) {
    return null;
  }


}
