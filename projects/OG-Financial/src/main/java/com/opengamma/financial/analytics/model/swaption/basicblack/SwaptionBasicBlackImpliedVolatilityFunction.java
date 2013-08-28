/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption.basicblack;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.black.ConstantBlackDiscountingImpliedVolatilitySwaptionFunction;

/**
 * Function that returns the implied volatility of a swaption. There are no volatility modelling
 * assumptions made for basic Black functions - the implied volatility is read directly from the
 * market data system.
 * <p>
 * Produces a result for {@link ValueRequirementNames#SECURITY_IMPLIED_VOLATILITY}.
 * @deprecated Use {@link ConstantBlackDiscountingImpliedVolatilitySwaptionFunction}
 */
@Deprecated
public class SwaptionBasicBlackImpliedVolatilityFunction extends SwaptionBasicBlackFunction {

  /**
   * Sets {@link ValueRequirementNames#SECURITY_IMPLIED_VOLATILITY} as the result.
   */
  public SwaptionBasicBlackImpliedVolatilityFunction() {
    super(ValueRequirementNames.SECURITY_IMPLIED_VOLATILITY);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative swaption, final YieldCurveWithBlackSwaptionBundle data, final ValueSpecification spec) {
    final Double iv = data.getBlackParameters().getVolatility(0, 0);
    return Collections.singleton(new ComputedValue(spec, iv));
  }
}
