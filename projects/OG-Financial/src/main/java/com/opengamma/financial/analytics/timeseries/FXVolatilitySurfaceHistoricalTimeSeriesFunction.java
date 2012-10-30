/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;

/**
 * 
 */
public class FXVolatilitySurfaceHistoricalTimeSeriesFunction extends VolatilitySurfaceHistoricalTimeSeriesFunction {

  @Override
  protected String getInstrumentType() {
    return InstrumentTypeProperties.FOREX;
  }

  @Override
  protected String getDataField() {
    return MarketDataRequirementNames.MARKET_VALUE;
  }
}
