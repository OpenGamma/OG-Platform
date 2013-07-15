/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata.availability;

import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityFilter;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Constructs a {@link MarketDataAvailabilityFilter} which reflects Bloomberg
 */
public class BloombergLiveDataAvailabilityFilterFactoryBean extends SingletonFactoryBean<MarketDataAvailabilityFilter> {

  @Override
  protected MarketDataAvailabilityFilter createObject() {
    return BloombergDataUtils.createAvailabilityFilter();
  }

}
