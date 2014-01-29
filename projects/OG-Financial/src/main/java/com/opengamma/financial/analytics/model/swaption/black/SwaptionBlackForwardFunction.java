/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption.black;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionBlackForwardCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Calculates the par rate of the underlying of a swaption using {@link SwaptionBlackForwardCalculator}
 * @deprecated The parent of this function is deprecated
 */
@Deprecated
public class SwaptionBlackForwardFunction extends SwaptionBlackFunction {
  /** The calculator */
  private static final SwaptionBlackForwardCalculator CALCULATOR = SwaptionBlackForwardCalculator.getInstance();

  /**
   * Sets the value requirement to {@link ValueRequirementNames#FORWARD}
   */
  public SwaptionBlackForwardFunction() {
    super(ValueRequirementNames.FORWARD);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative swaption, final YieldCurveWithBlackSwaptionBundle data, final ValueSpecification spec) {
    final Double pv = swaption.accept(CALCULATOR, data);
    return Collections.singleton(new ComputedValue(spec, pv));
  }

  @Override
  protected ValueProperties getResultProperties(final String currency) {
    return super.getResultProperties(currency)
        .withoutAny(ValuePropertyNames.CURRENCY);
  }

  @Override
  protected ValueProperties getResultProperties(final String currency, final String curveCalculationConfigName, final String surfaceName) {
    return super.getResultProperties(currency, curveCalculationConfigName, surfaceName)
        .withoutAny(ValuePropertyNames.CURRENCY);
  }
}
