/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.production.server;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.core.value.MarketDataRequirementNamesHelper;
import com.opengamma.engine.marketdata.availability.DomainMarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.SingletonFactoryBean;

/**
 * 
 */
public class DemoLiveDataAvailabilityProviderFactoryBean extends SingletonFactoryBean<MarketDataAvailabilityProvider> {

  private final SecuritySource _securitySource;
  
  public DemoLiveDataAvailabilityProviderFactoryBean(SecuritySource securitySource) {
    _securitySource = securitySource;
  }
  
  @Override
  protected MarketDataAvailabilityProvider createObject() {
    Collection<ExternalScheme> acceptableSchemes = new HashSet<ExternalScheme>();
    Collections.addAll(acceptableSchemes, SecurityUtils.BLOOMBERG_BUID_WEAK, SecurityUtils.BLOOMBERG_BUID, SecurityUtils.BLOOMBERG_TICKER_WEAK, SecurityUtils.BLOOMBERG_TICKER);
    Collection<String> validMarketDataRequirementNames = MarketDataRequirementNamesHelper.constructValidRequirementNames();
    return new DomainMarketDataAvailabilityProvider(_securitySource, acceptableSchemes, validMarketDataRequirementNames);
  }

}
