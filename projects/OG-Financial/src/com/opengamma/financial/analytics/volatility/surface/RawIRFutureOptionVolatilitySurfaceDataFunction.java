/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class RawIRFutureOptionVolatilitySurfaceDataFunction extends RawVolatilitySurfaceDataFunction {

  public RawIRFutureOptionVolatilitySurfaceDataFunction() {
    super(InstrumentTypeProperties.IR_FUTURE_OPTION);
  }

  @Override
  public boolean isCorrectIdType(final ComputationTarget target) {
    return Currency.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
  }

}
