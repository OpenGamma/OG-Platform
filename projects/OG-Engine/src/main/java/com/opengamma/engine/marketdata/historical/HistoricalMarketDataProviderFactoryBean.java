/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Spring factory bean for {@link HistoricalMarketDataProvider}.
 */
public class HistoricalMarketDataProviderFactoryBean extends SingletonFactoryBean<AbstractHistoricalMarketDataProvider> {

  private HistoricalTimeSeriesSource _historicalTimeSeriesSource;
  private SecuritySource _securitySource;
  
  public HistoricalTimeSeriesSource getHistoricalTimeSeriesSource() {
    return _historicalTimeSeriesSource;
  }

  public void setHistoricalTimeSeriesSource(HistoricalTimeSeriesSource historicalTimeSeriesSource) {
    _historicalTimeSeriesSource = historicalTimeSeriesSource;
  }
  
  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  public void setSecuritySource(SecuritySource securitySource) {
    _securitySource = securitySource;
  }

  @Override
  protected AbstractHistoricalMarketDataProvider createObject() {
    ArgumentChecker.notNullInjected(getHistoricalTimeSeriesSource(), "historicalTimeSeriesSource");
    return new HistoricalMarketDataProvider(getHistoricalTimeSeriesSource(), getSecuritySource());
  }

}
