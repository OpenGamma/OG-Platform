/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.distribution;

import java.util.Collection;

/**
 * Creates senders.
 */
public interface MarketDataSenderFactory {

  /**
   * Creates market data senders that will send market data to clients.
   * 
   * @param distributor  the distributor to use, not null
   * @return the collection of senders, not null
   */
  Collection<MarketDataSender> create(MarketDataDistributor distributor);

}
