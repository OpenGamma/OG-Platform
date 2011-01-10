/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.distribution;

import java.util.Collection;
import java.util.Collections;

/**
 * Use this {@code MarketDataSenderFactory} if no messages should be distributed to clients. 
 * Useful in tests.
 *
 * @author pietari
 */
public class EmptyMarketDataSenderFactory implements MarketDataSenderFactory {

  @Override
  public Collection<MarketDataSender> create(MarketDataDistributor distributor) {
    return Collections.emptyList();
  }

}
