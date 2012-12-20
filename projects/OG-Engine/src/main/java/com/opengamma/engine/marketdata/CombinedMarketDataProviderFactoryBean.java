/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Spring factory bean for {@link CombinedMarketDataProvider}.
 */
public class CombinedMarketDataProviderFactoryBean extends SingletonFactoryBean<CombinedMarketDataProvider> {

  private MarketDataProvider _preferredProvider;
  private MarketDataProvider _fallbackProvider;

  public MarketDataProvider getPreferredProvider() {
    return _preferredProvider;
  }

  public void setPreferredProvider(MarketDataProvider preferredProvider) {
    _preferredProvider = preferredProvider;
  }

  public MarketDataProvider getFallbackProvider() {
    return _fallbackProvider;
  }

  public void setFallbackProvider(MarketDataProvider fallbackProvider) {
    _fallbackProvider = fallbackProvider;
  }

  //-------------------------------------------------------------------------
  @Override
  protected CombinedMarketDataProvider createObject() {
    ArgumentChecker.notNullInjected(getPreferredProvider(), "preferredProvider");
    ArgumentChecker.notNullInjected(getFallbackProvider(), "fallbackProvider");
    return new CombinedMarketDataProvider(getPreferredProvider(), getFallbackProvider());
  }

}
