/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata.availability;

import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Constructs a {@link MarketDataAvailabilityProvider} which reflects Bloomberg
 */
public class BloombergLiveDataAvailabilityProviderFactoryBean extends SingletonFactoryBean<MarketDataAvailabilityProvider> {

  private SecuritySource _securitySource;
  
  public BloombergLiveDataAvailabilityProviderFactoryBean() {
  }
  
  public BloombergLiveDataAvailabilityProviderFactoryBean(SecuritySource securitySource) {
    _securitySource = securitySource;
  }
  
  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  public void setSecuritySource(SecuritySource securitySource) {
    _securitySource = securitySource;
  }

  @Override
  protected MarketDataAvailabilityProvider createObject() {
    ArgumentChecker.notNullInjected(getSecuritySource(), "securitySource");
    return BloombergDataUtils.createAvailabilityProvider(getSecuritySource());
  }
}
