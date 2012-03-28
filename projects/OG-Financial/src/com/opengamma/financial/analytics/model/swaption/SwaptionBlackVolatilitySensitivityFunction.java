/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.PresentValueBlackSwaptionSensitivity;
import com.opengamma.financial.interestrate.PresentValueBlackSwaptionSensitivityBlackCalculator;
import com.opengamma.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class SwaptionBlackVolatilitySensitivityFunction extends SwaptionBlackFunction {
  private static final PresentValueBlackSwaptionSensitivityBlackCalculator CALCULATOR = PresentValueBlackSwaptionSensitivityBlackCalculator.getInstance();

  public SwaptionBlackVolatilitySensitivityFunction() {
    super(ValueRequirementNames.VALUE_VEGA);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative swaption, final YieldCurveWithBlackSwaptionBundle data, final ValueSpecification spec) {
    final PresentValueBlackSwaptionSensitivity sensitivities = swaption.accept(CALCULATOR, data);
    final HashMap<DoublesPair, Double> result = sensitivities.getSensitivity().getMap();
    if (result.size() != 1) {
      throw new OpenGammaRuntimeException("Expecting only one result for Black value vega");
    }
    return Collections.singleton(new ComputedValue(spec, result.values().iterator().next()));
  }
}
