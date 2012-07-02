/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class RawIRFutureOptionVolatilitySurfaceDataFunction extends RawVolatilitySurfaceDataFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(RawIRFutureOptionVolatilitySurfaceDataFunction.class);

  public RawIRFutureOptionVolatilitySurfaceDataFunction() {
    super(InstrumentTypeProperties.IR_FUTURE_OPTION);
  }

  @Override
  public boolean isCorrectIdType(final ComputationTarget target) {
    if (target.getUniqueId() == null) {
      s_logger.error("Target unique id was null; {}", target);
      return false;
    }
    return Currency.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
  }

}
