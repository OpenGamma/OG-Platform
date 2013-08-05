/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.ParRateParallelSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Function that calculates the sensitivity of the par rate to a parallel curve shift.
 * @deprecated The parent function is deprecated
 */
@Deprecated
public class InterestRateInstrumentParRateParallelCurveSensitivityFunction extends InterestRateInstrumentCurveSpecificFunction {
  private static final ParRateParallelSensitivityCalculator CALCULATOR = ParRateParallelSensitivityCalculator.getInstance();

  public InterestRateInstrumentParRateParallelCurveSensitivityFunction() {
    super(ValueRequirementNames.PAR_RATE_PARALLEL_CURVE_SHIFT);
  }

  @Override
  public Set<ComputedValue> getResults(final InstrumentDerivative derivative, final String curveName, final YieldCurveBundle curves,
      final String curveCalculationConfigName, final String curveCalculationMethod, final FunctionInputs inputs, final ComputationTarget target,
      final ValueSpecification resultSpec) {
    final Map<String, Double> sensitivities = CALCULATOR.visit(derivative, curves);
    if (!sensitivities.containsKey(curveName)) {
      throw new OpenGammaRuntimeException("Could not get par rate parallel curve shift sensitivity for curve named " + curveName + "; should never happen");
    }
    return Collections.singleton(new ComputedValue(resultSpec, sensitivities.get(curveName)));
  }

}
