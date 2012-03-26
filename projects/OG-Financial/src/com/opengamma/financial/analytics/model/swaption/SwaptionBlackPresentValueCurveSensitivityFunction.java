/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.PresentValueCurveSensitivityBlackSwaptionCalculator;
import com.opengamma.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class SwaptionBlackPresentValueCurveSensitivityFunction {
  private static final PresentValueCurveSensitivityBlackSwaptionCalculator CALCULATOR = PresentValueCurveSensitivityBlackSwaptionCalculator.getInstance();

  protected Set<ComputedValue> getResult(final InstrumentDerivative swaption, final YieldCurveWithBlackSwaptionBundle data, final ValueSpecification spec) {
    final Map<String, List<DoublesPair>> sensitivities = swaption.accept(CALCULATOR, data);
    return Collections.singleton(new ComputedValue(spec, sensitivities));
  }
}
