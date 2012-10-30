/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
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
public class RawSwaptionATMVolatilitySurfaceDataFunction extends RawVolatilitySurfaceDataFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(RawSwaptionATMVolatilitySurfaceDataFunction.class);
  public RawSwaptionATMVolatilitySurfaceDataFunction() {
    super(InstrumentTypeProperties.SWAPTION_ATM);
  }

  @Override
  protected ComputationTargetType getTargetType() {
    return ComputationTargetType.CURRENCY;
  }

}
