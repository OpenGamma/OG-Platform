/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.PresentValueBlackSwaptionSensitivity;
import com.opengamma.financial.interestrate.PresentValueBlackSwaptionSensitivityBlackSwaptionCalculator;
import com.opengamma.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;

/**
 * 
 */
public class SwaptionBlackVolatilitySensitivityFunction {
  private static final PresentValueBlackSwaptionSensitivityBlackSwaptionCalculator CALCULATOR = PresentValueBlackSwaptionSensitivityBlackSwaptionCalculator.getInstance();

  protected Set<ComputedValue> getResult(final InstrumentDerivative swaption, final YieldCurveWithBlackSwaptionBundle data, final ValueSpecification spec) {
    final PresentValueBlackSwaptionSensitivity sensitivities = swaption.accept(CALCULATOR, data);
    return Collections.singleton(new ComputedValue(spec, sensitivities));
  }
}
