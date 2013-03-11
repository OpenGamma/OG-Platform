/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.server;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNamesHelper;
import com.opengamma.engine.marketdata.availability.DomainMarketDataAvailabilityFilter;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityFilter;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.SingletonFactoryBean;

/**
 *
 */
public class DemoLiveDataAvailabilityFilterFactoryBean extends SingletonFactoryBean<MarketDataAvailabilityFilter> {

  @Override
  protected MarketDataAvailabilityFilter createObject() {
    final Collection<ExternalScheme> acceptableSchemes = new HashSet<ExternalScheme>();
    Collections.addAll(acceptableSchemes, ExternalSchemes.BLOOMBERG_BUID_WEAK, ExternalSchemes.BLOOMBERG_BUID, ExternalSchemes.BLOOMBERG_TICKER_WEAK, ExternalSchemes.BLOOMBERG_TICKER);
    final Collection<String> validMarketDataRequirementNames = MarketDataRequirementNamesHelper.constructValidRequirementNames();
    return new DomainMarketDataAvailabilityFilter(acceptableSchemes, validMarketDataRequirementNames);
  }

}
