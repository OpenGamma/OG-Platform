/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption.black;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionBlackValueGammaCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Calculates value gamma of swaptions using the Black method.
 * @deprecated The parent class of this function is deprecated
 */
@Deprecated
public class SwaptionBlackValueGammaFunction extends SwaptionBlackFunction {
  /** The calculator */
  private static final SwaptionBlackValueGammaCalculator CALCULATOR = SwaptionBlackValueGammaCalculator.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#VALUE_GAMMA}
   */
  public SwaptionBlackValueGammaFunction() {
    super(ValueRequirementNames.VALUE_GAMMA);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative swaption, final YieldCurveWithBlackSwaptionBundle data, final ValueSpecification spec) {
    final CurrencyAmount result = swaption.accept(CALCULATOR, data);
    final String resultCurrency = result.getCurrency().getCode();
    final String expectedCurrency = spec.getProperty(ValuePropertyNames.CURRENCY);
    if (!expectedCurrency.equals(resultCurrency)) {
      throw new OpenGammaRuntimeException("Expected currency " + expectedCurrency + " does not equal result currency " + resultCurrency);
    }
    final double gammaValue = result.getAmount();
    return Collections.singleton(new ComputedValue(spec, gammaValue));
  }
}
