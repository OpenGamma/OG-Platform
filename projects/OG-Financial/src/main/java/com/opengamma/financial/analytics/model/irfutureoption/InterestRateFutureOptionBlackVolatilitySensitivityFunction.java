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
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.black.BlackDiscountingValueVegaIRFutureOptionFunction;
import com.opengamma.util.tuple.DoublesPair;

/**
 * @deprecated Use {@link BlackDiscountingValueVegaIRFutureOptionFunction}
 */
@Deprecated
public class InterestRateFutureOptionBlackVolatilitySensitivityFunction extends InterestRateFutureOptionBlackFunction {
  /** The calculator */
  private static final PresentValueBlackSensitivityBlackCalculator CALCULATOR = PresentValueBlackSensitivityBlackCalculator.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#VALUE_VEGA}
   */
  public InterestRateFutureOptionBlackVolatilitySensitivityFunction() {
    super(ValueRequirementNames.VALUE_VEGA, true);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative irFutureOption, final YieldCurveWithBlackCubeBundle data, final ValueSpecification spec,
      final Set<ValueRequirement> desiredValues) {
    final SurfaceValue sensitivities = irFutureOption.accept(CALCULATOR, data);
    final HashMap<DoublesPair, Double> result = sensitivities.getMap();
    if (result.size() != 1) {
      throw new OpenGammaRuntimeException("Expecting only one result for Black value vega");
    }
    return Collections.singleton(new ComputedValue(spec, result.values().iterator().next() / 100.0));
  }

}
