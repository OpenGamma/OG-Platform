/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.marketdata;

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
  public boolean isAvailable(ValueRequirement requirement) {
    MockMarketDataProviderFactoryBean.printWarning();
    return false;
  }

}
