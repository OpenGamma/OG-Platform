/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;

/**
 * 
 */
public class RawIRFutureOptionVolatilitySurfaceDataFunction extends RawVolatilitySurfaceDataFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(RawIRFutureOptionVolatilitySurfaceDataFunction.class);

  public RawIRFutureOptionVolatilitySurfaceDataFunction() {
    super(InstrumentTypeProperties.IR_FUTURE_OPTION);
  }

  @Override
  protected ComputationTargetType getTargetType() {
    return ComputationTargetType.CURRENCY;
  }

}
