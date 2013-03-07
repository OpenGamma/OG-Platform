/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Spring factory bean for {@link HistoricalMarketDataProvider}.
 */
public class HistoricalMarketDataProviderFactoryBean extends SingletonFactoryBean<AbstractHistoricalMarketDataProvider> {

  private HistoricalTimeSeriesSource _historicalTimeSeriesSource;
  private HistoricalTimeSeriesResolver _historicalTimeSeriesResolver;

  public HistoricalTimeSeriesSource getHistoricalTimeSeriesSource() {
    return _historicalTimeSeriesSource;
  }

  public void setHistoricalTimeSeriesSource(final HistoricalTimeSeriesSource historicalTimeSeriesSource) {
    _historicalTimeSeriesSource = historicalTimeSeriesSource;
  }

  public HistoricalTimeSeriesResolver getHistoricalTimeSeriesResolver() {
    return _historicalTimeSeriesResolver;
  }

  public void setHistoricalTimeSeriesResolver(final HistoricalTimeSeriesResolver historicalTimeSeriesResolver) {
    _historicalTimeSeriesResolver = historicalTimeSeriesResolver;
  }

  @Override
  protected AbstractHistoricalMarketDataProvider createObject() {
    ArgumentChecker.notNullInjected(getHistoricalTimeSeriesSource(), "historicalTimeSeriesSource");
    ArgumentChecker.notNullInjected(getHistoricalTimeSeriesResolver(), "historicalTimeSeriesResolver");
    return new HistoricalMarketDataProvider(getHistoricalTimeSeriesSource(), getHistoricalTimeSeriesResolver());
  }

}
