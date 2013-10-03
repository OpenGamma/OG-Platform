/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption.black;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionBlackForwardDeltaCalculator;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionBlackForwardThetaCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.black.BlackDiscountingPVSwaptionFunction;

/**
 * Calculates theta of swaptions using the Black method.
 * @deprecated The parent class is deprecated
 */
@Deprecated
public class SwaptionBlackTheoreticalForwardThetaFunction extends SwaptionBlackFunction  {
  /** The forward theta calculator */
  private static final SwaptionBlackForwardThetaCalculator CALCULATOR = SwaptionBlackForwardThetaCalculator.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#THETA}
   */
  public SwaptionBlackTheoreticalForwardThetaFunction() {
    super(ValueRequirementNames.THETA);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative swaption, final YieldCurveWithBlackSwaptionBundle data, final ValueSpecification spec) {
    final Double result = swaption.accept(CALCULATOR, data);
    return Collections.singleton(new ComputedValue(spec, result));
  }
}
