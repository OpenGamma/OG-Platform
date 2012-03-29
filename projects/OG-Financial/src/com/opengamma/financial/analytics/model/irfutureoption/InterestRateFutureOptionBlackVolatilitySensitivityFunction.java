/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackSensitivityBlackCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.analytics.util.surface.SurfaceValue;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class InterestRateFutureOptionBlackVolatilitySensitivityFunction extends InterestRateFutureOptionBlackFunction {
  private static final PresentValueBlackSensitivityBlackCalculator CALCULATOR = PresentValueBlackSensitivityBlackCalculator.getInstance();

  public InterestRateFutureOptionBlackVolatilitySensitivityFunction() {
    super(ValueRequirementNames.VALUE_VEGA);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative irFutureOption, final YieldCurveWithBlackCubeBundle data, final ValueSpecification spec) {
    final SurfaceValue sensitivities = irFutureOption.accept(CALCULATOR, data);
    final HashMap<DoublesPair, Double> result = sensitivities.getMap();
    if (result.size() != 1) {
      throw new OpenGammaRuntimeException("Expecting only one result for Black value vega");
    }
    return Collections.singleton(new ComputedValue(spec, result.values().iterator().next()));
  }

}
