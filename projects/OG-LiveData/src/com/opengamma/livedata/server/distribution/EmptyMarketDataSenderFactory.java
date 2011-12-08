/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.distribution;

import java.util.Collection;
import java.util.Collections;

/**
 * Use this {@code MarketDataSenderFactory} if no messages should be distributed to clients. 
 * Useful in tests.
 */
public class EmptyMarketDataSenderFactory implements MarketDataSenderFactory {

  @Override
  public Collection<MarketDataSender> create(MarketDataDistributor distributor) {
    return Collections.emptyList();
  }

}
