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

/**
 * 
 */
public class SwaptionBasicBlackImpliedVolatilityFunction extends SwaptionBasicBlackFunction {

  public SwaptionBasicBlackImpliedVolatilityFunction() {
    super(ValueRequirementNames.SECURITY_IMPLIED_VOLATILITY);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative swaption, final YieldCurveWithBlackSwaptionBundle data, final ValueSpecification spec) {
    final Double iv = data.getBlackParameters().getVolatility(0, 0);
    return Collections.singleton(new ComputedValue(spec, iv));
  }
}
