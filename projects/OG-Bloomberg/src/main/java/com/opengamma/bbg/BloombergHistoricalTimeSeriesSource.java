/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import com.opengamma.id.UniqueIdSupplier;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.provider.historicaltimeseries.impl.MarketDataProviderHistoricalTimeSeriesSource;

/**
 * Loads time-series from Bloomberg.
 * This class is now implemented on top of HistoricalTimeSeriesProvider and is effectively deprecated. 
 */
public class BloombergHistoricalTimeSeriesSource extends MarketDataProviderHistoricalTimeSeriesSource {

  /**
   * Creates an instance.
   * 
   * @param provider  the time-series provider, not null
   */
  public BloombergHistoricalTimeSeriesSource(HistoricalTimeSeriesProvider provider) {
    super("Bloomberg", new UniqueIdSupplier("BbgHTS"), provider);
  }

}
