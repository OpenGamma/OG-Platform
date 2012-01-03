/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesFields;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;

/**
 * Convert market data value requirement names ({@link MarketDataRequirementNames}) to field names ({@link HistoricalTimeSeriesFields}).
 */
public interface HistoricalMarketDataFieldResolver {

  /**
   * Resolves a value name to the time series field name.
   * 
   * @param valueName to resolve, e.g. "Market_Value", not null
   * @param resolutionKey resolution key to control the strategy for the resolution. This is resolver specific, perhaps a DSL or name
   * of a config document. Use null for the resolver's default strategy.
   * @return the time series field name, e.g. "PX_LAST", or null if there is no mapping
   */
  String resolve(String valueName, String resolutionKey);

}
