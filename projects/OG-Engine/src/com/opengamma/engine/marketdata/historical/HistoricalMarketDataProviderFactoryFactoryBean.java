/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Spring factory bean for {@link HistoricalMarketDataProviderFactory}.
 */
public class HistoricalMarketDataProviderFactoryFactoryBean extends SingletonFactoryBean<HistoricalMarketDataProviderFactory> {

  private HistoricalTimeSeriesSource _timeSeriesSource;
  private HistoricalMarketDataFieldResolver _fieldResolver;
  private HistoricalMarketDataNormalizer _normalizer = new IdentityDataNormalizer();
  
  public HistoricalTimeSeriesSource getTimeSeriesSource() {
    return _timeSeriesSource;
  }

  public void setTimeSeriesSource(HistoricalTimeSeriesSource timeSeriesSource) {
    _timeSeriesSource = timeSeriesSource;
  }

  public HistoricalMarketDataFieldResolver getFieldResolver() {
    return _fieldResolver;
  }

  public void setFieldResolver(final HistoricalMarketDataFieldResolver fieldResolver) {
    _fieldResolver = fieldResolver;
  }

  public HistoricalMarketDataNormalizer getNormalizer() {
    return _normalizer;
  }

  public void setNormalizer(final HistoricalMarketDataNormalizer normalizer) {
    _normalizer = normalizer;
  }

  @Override
  protected HistoricalMarketDataProviderFactory createObject() {
    ArgumentChecker.notNullInjected(getTimeSeriesSource(), "timeSeriesSource");
    ArgumentChecker.notNullInjected(getFieldResolver(), "fieldResolver");
    ArgumentChecker.notNullInjected(getNormalizer(), "normalizer");
    return new HistoricalMarketDataProviderFactory(getTimeSeriesSource(), getFieldResolver(), getNormalizer());
  }

}
