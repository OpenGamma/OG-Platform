/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption.black;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionBlackDriftlessThetaCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Calculates driftless theta of swaptions using the Black method.
 * @deprecated The parent class is deprecated
 */
@Deprecated
public class SwaptionBlackDriftlessThetaFunction extends SwaptionBlackFunction  {
  /** The calculator */
  private static final SwaptionBlackDriftlessThetaCalculator CALCULATOR = SwaptionBlackDriftlessThetaCalculator.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#DRIFTLESS_THETA}
   */
  public SwaptionBlackDriftlessThetaFunction() {
    super(ValueRequirementNames.DRIFTLESS_THETA);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative swaption, final YieldCurveWithBlackSwaptionBundle data, final ValueSpecification spec) {
    final Double result = swaption.accept(CALCULATOR, data);
    return Collections.singleton(new ComputedValue(spec, result));
  }
}
