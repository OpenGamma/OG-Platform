/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.marketdata.spec.HistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;

/**
 * A factory for {@link HistoricalMarketDataProvider} instances.
 */
public class HistoricalMarketDataProviderFactory implements MarketDataProviderFactory {

  private final HistoricalTimeSeriesSource _timeSeriesSource;
  
  public HistoricalMarketDataProviderFactory(HistoricalTimeSeriesSource timeSeriesSource) {
    _timeSeriesSource = timeSeriesSource;
  }
  
  @Override
  public MarketDataProvider create(MarketDataSpecification marketDataSpec) {
    HistoricalMarketDataSpecification historicalMarketDataSpec = (HistoricalMarketDataSpecification) marketDataSpec;
    return new HistoricalMarketDataProvider(getTimeSeriesSource(), historicalMarketDataSpec.getDataSource(), historicalMarketDataSpec.getDataProvider(), historicalMarketDataSpec.getDataField());
  }
  
  //-------------------------------------------------------------------------
  private HistoricalTimeSeriesSource getTimeSeriesSource() {
    return _timeSeriesSource;
  }

}
