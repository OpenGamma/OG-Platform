/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.marketdata;

import com.opengamma.engine.marketdata.availability.MarketDataAvailability;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.value.ValueRequirement;

/**
 * Mock live data availability provider to allow the example server to run.
 * 
 * For fully-supported implementations supporting major data vendors like Bloomberg and Thomson-Reuters, please contact
 * sales@opengamma.com
 */
public class MockMarketDataAvailabilityProvider implements MarketDataAvailabilityProvider {

  @Override
  public MarketDataAvailability getAvailability(final ValueRequirement requirement) {
    MockMarketDataProviderFactoryBean.printWarning();
    return MarketDataAvailability.NOT_AVAILABLE;
  }

}
