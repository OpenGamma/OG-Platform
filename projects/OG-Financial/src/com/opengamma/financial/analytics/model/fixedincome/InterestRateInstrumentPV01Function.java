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
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.PV01Calculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;

/**
 * 
 */
public class InterestRateInstrumentPV01Function extends InterestRateInstrumentCurveSpecificFunction {
  private static final PV01Calculator CALCULATOR = PV01Calculator.getInstance();

  public InterestRateInstrumentPV01Function() {
    super(ValueRequirementNames.PV01);
  }

  @Override
  public Set<ComputedValue> getResults(final InstrumentDerivative derivative, final String curveName, final InterpolatedYieldCurveSpecificationWithSecurities curveSpec,
      final YieldCurveBundle curves, final ValueSpecification resultSpec) {
    final Map<String, Double> pv01 = CALCULATOR.visit(derivative, curves);
    if (!pv01.containsKey(curveName)) {
      throw new OpenGammaRuntimeException("Could not get PV01 for curve named " + curveName + "; should never happen");
    }
    return Collections.singleton(new ComputedValue(resultSpec, pv01.get(curveName)));
  }
}
