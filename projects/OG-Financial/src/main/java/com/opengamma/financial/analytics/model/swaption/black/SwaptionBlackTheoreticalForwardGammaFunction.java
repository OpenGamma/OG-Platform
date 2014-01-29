/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption.black;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionBlackForwardGammaCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Calculates gamma of swaptions using the Black method.
 * @deprecated The parent class of this function is deprecated
 */
@Deprecated
public class SwaptionBlackTheoreticalForwardGammaFunction extends SwaptionBlackFunction  {
  /** The calculator */
  private static final SwaptionBlackForwardGammaCalculator CALCULATOR = SwaptionBlackForwardGammaCalculator.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#FORWARD_GAMMA}
   */
  public SwaptionBlackTheoreticalForwardGammaFunction() {
    super(ValueRequirementNames.FORWARD_GAMMA);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative swaption, final YieldCurveWithBlackSwaptionBundle data, final ValueSpecification spec) {
    final Double result = swaption.accept(CALCULATOR, data);
    return Collections.singleton(new ComputedValue(spec, result / 100000000.0));
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
