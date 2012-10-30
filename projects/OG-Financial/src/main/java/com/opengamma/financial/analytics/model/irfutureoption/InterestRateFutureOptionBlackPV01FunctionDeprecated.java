/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PV01Calculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityBlackCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * @deprecated Use the version of the function that does not refer to funding and forward curves
 * @see InterestRateFutureOptionBlackPV01Function
 */
@Deprecated
public class InterestRateFutureOptionBlackPV01FunctionDeprecated extends InterestRateFutureOptionBlackCurveSpecificFunctionDeprecated {
  private static final PV01Calculator CALCULATOR = new PV01Calculator(PresentValueCurveSensitivityBlackCalculator.getInstance());

  public InterestRateFutureOptionBlackPV01FunctionDeprecated() {
    super(ValueRequirementNames.PV01);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative irFutureOption, final YieldCurveWithBlackCubeBundle data, final String curveName, final ValueSpecification spec) {
    final Map<String, Double> pv01 = CALCULATOR.visit(irFutureOption, data);
    if (!pv01.containsKey(curveName)) {
      throw new OpenGammaRuntimeException("Could not get PV01 for " + curveName);
    }
    return Collections.singleton(new ComputedValue(spec, pv01.get(curveName)));
  }

}
