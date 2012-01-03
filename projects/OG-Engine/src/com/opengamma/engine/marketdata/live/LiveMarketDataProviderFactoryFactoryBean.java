/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataProviderFactory;
import com.opengamma.engine.marketdata.SingletonMarketDataProviderFactory;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Spring factory bean for {@link LiveMarketDataProvider}.
 */
public class LiveMarketDataProviderFactoryFactoryBean extends SingletonFactoryBean<MarketDataProviderFactory> {

  private LiveDataClient _liveDataClient;
  private SecuritySource _securitySource;
  private MarketDataAvailabilityProvider _availabilityProvider;
  
  public LiveDataClient getLiveDataClient() {
    return _liveDataClient;
  }
  
  public void setLiveDataClient(LiveDataClient liveDataClient) {
    _liveDataClient = liveDataClient;
  }

  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  public void setSecuritySource(SecuritySource securitySource) {
    _securitySource = securitySource;
  }

  public MarketDataAvailabilityProvider getAvailabilityProvider() {
    return _availabilityProvider;
  }

  public void setAvailabilityProvider(MarketDataAvailabilityProvider availabilityProvider) {
    _availabilityProvider = availabilityProvider;
  }

  @Override
  protected MarketDataProviderFactory createObject() {
    ArgumentChecker.notNullInjected(getLiveDataClient(), "liveDataClient");
    ArgumentChecker.notNullInjected(getSecuritySource(), "securitySource");
    ArgumentChecker.notNullInjected(getAvailabilityProvider(), "availabilityProvider");
    MarketDataProvider provider = new LiveMarketDataProvider(getLiveDataClient(), getSecuritySource(), getAvailabilityProvider());
    return new SingletonMarketDataProviderFactory(provider);
  }

}
