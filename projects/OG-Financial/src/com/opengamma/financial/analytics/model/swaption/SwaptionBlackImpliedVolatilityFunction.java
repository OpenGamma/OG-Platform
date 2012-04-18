/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.ImpliedVolatilityBlackCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Function to compute the implied volatility for physical delivery swaptions in the Black model.
 */
public class SwaptionBlackImpliedVolatilityFunction extends SwaptionBlackFunction {

  /**
   * The related calculator.
   */
  private static final ImpliedVolatilityBlackCalculator CALCULATOR = ImpliedVolatilityBlackCalculator.getInstance();

  public SwaptionBlackImpliedVolatilityFunction() {
    super(ValueRequirementNames.SECURITY_IMPLIED_VOLATLITY);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative swaption, final YieldCurveWithBlackSwaptionBundle data, final ValueSpecification spec) {
    final Double iv = swaption.accept(CALCULATOR, data);
    return Collections.singleton(new ComputedValue(spec, iv));
  }
}
