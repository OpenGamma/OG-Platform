/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.server;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.engine.livedata.DomainLiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.livedata.normalization.MarketDataRequirementNamesHelper;
import com.opengamma.util.SingletonFactoryBean;

/**
 * 
 */
public class DemoLiveDataAvailabilityProviderFactoryBean extends SingletonFactoryBean<LiveDataAvailabilityProvider> {

  private final SecuritySource _securityMaster;
  
  public DemoLiveDataAvailabilityProviderFactoryBean(SecuritySource securityMaster) {
    _securityMaster = securityMaster;
  }
  
  @Override
  protected LiveDataAvailabilityProvider createObject() {
    Collection<IdentificationScheme> acceptableSchemes = new HashSet<IdentificationScheme>();
    Collections.addAll(acceptableSchemes, SecurityUtils.BLOOMBERG_BUID, SecurityUtils.BLOOMBERG_TICKER);
    Collection<String> validMarketDataRequirementNames = MarketDataRequirementNamesHelper.constructValidRequirementNames();
    return new DomainLiveDataAvailabilityProvider(_securityMaster, acceptableSchemes, validMarketDataRequirementNames);
  }

}
