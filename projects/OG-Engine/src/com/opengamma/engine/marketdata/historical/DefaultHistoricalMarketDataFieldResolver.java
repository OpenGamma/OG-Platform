/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesFields;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.ArgumentChecker;

/**
 * Default implementation of {@link HistoricalMarketDataFieldResolver} using a static map of value names
 * to time series field names. The resolutionKey parameter is always ignored.
 */
public class DefaultHistoricalMarketDataFieldResolver implements HistoricalMarketDataFieldResolver {

  private final Map<String, String> _resolutionMap;

  /**
   * Creates a default resolver instance.
   */
  public DefaultHistoricalMarketDataFieldResolver() {
    this(createDefaultMap());
  }

  /**
   * Creates a resolver based on a static map.
   * 
   * @param resolutionMap the underlying map to use, not null
   */
  protected DefaultHistoricalMarketDataFieldResolver(final Map<String, String> resolutionMap) {
    ArgumentChecker.notNull(resolutionMap, "resolutionMap");
    _resolutionMap = resolutionMap;
  }

  /**
   * Creates the map of resolutions used by the default instance.
   * 
   * @return the default map
   */
  protected static Map<String, String> createDefaultMap() {
    final Map<String, String> map = new HashMap<String, String>();
    map.put(MarketDataRequirementNames.MARKET_VALUE, HistoricalTimeSeriesFields.LAST_PRICE);
    map.put(MarketDataRequirementNames.VOLUME, HistoricalTimeSeriesFields.VOLUME);
    map.put(MarketDataRequirementNames.YIELD_CONVENTION_MID, HistoricalTimeSeriesFields.YIELD_TO_CONVENTION);
    map.put(MarketDataRequirementNames.YIELD_YIELD_TO_MATURITY_MID, HistoricalTimeSeriesFields.YIELD_TO_MATURITY);
    return map;
  }

  /**
   * Returns the underlying map used by {@link #resolve}.
   * 
   * @return the map
   */
  protected Map<String, String> getResolutionMap() {
    return _resolutionMap;
  }

  @Override
  public String resolve(final String valueName, final String resolutionKey) {
    return getResolutionMap().get(valueName);
  }
}
