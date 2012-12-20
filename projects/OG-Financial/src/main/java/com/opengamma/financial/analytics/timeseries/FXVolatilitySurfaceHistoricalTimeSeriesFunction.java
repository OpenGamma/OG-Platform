/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * 
 */
public class FXVolatilitySurfaceHistoricalTimeSeriesFunction extends VolatilitySurfaceHistoricalTimeSeriesFunction {

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final UniqueId uid = target.getUniqueId();
    return (uid != null) && uid.getScheme().equals(UnorderedCurrencyPair.OBJECT_SCHEME);
  }

  @Override
  protected String getInstrumentType() {
    return InstrumentTypeProperties.FOREX;
  }

  @Override
  protected String getDataField() {
    return MarketDataRequirementNames.MARKET_VALUE;
  }
}
