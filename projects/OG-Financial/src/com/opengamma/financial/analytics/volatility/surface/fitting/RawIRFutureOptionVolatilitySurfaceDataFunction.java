/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface.fitting;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.financial.analytics.volatility.surface.RawVolatilitySurfaceDataFunction;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class RawIRFutureOptionVolatilitySurfaceDataFunction extends RawVolatilitySurfaceDataFunction {

  public RawIRFutureOptionVolatilitySurfaceDataFunction(final String definitionName, final String specificationName) {
    super(definitionName, specificationName, "IR_FUTURE_OPTION");
  }

  @Override
  public boolean isCorrectIdType(final ComputationTarget target) {
    return Currency.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
  }

}
