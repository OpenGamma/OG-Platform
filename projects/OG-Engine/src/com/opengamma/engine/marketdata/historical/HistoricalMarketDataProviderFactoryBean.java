/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Spring factory bean for {@link HistoricalMarketDataProvider}.
 */
public class HistoricalMarketDataProviderFactoryBean extends SingletonFactoryBean<AbstractHistoricalMarketDataProvider> {

  private HistoricalTimeSeriesSource _historicalTimeSeriesSource;
  
  public HistoricalTimeSeriesSource getHistoricalTimeSeriesSource() {
    return _historicalTimeSeriesSource;
  }

  public void setHistoricalTimeSeriesSource(HistoricalTimeSeriesSource historicalTimeSeriesSource) {
    _historicalTimeSeriesSource = historicalTimeSeriesSource;
  }

  @Override
  protected AbstractHistoricalMarketDataProvider createObject() {
    ArgumentChecker.notNullInjected(getHistoricalTimeSeriesSource(), "historicalTimeSeriesSource");
    return new HistoricalMarketDataProvider(getHistoricalTimeSeriesSource());
  }

}
