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
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionBlackForwardVegaCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Calculates vega of swaptions using the Black method.
 * @deprecated The parent class of this function is deprecated
 */
@Deprecated
public class SwaptionBlackTheoreticalForwardVegaFunction extends SwaptionBlackFunction  {
  /** The forward vega calculator */
  private static final SwaptionBlackForwardVegaCalculator CALCULATOR = SwaptionBlackForwardVegaCalculator.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#FORWARD_VEGA}
   */
  public SwaptionBlackTheoreticalForwardVegaFunction() {
    super(ValueRequirementNames.FORWARD_VEGA);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative swaption, final YieldCurveWithBlackSwaptionBundle data, final ValueSpecification spec) {
    final Double result = swaption.accept(CALCULATOR, data);
    return Collections.singleton(new ComputedValue(spec, result));
  }
}
