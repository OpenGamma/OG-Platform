/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.distribution;

import java.util.Collection;

/**
 * Creates {@code MarketDataSender}'s.
 */
public interface MarketDataSenderFactory {
  
  /**
   * Creates market data senders that will send market data
   * to clients.
   * 
   * @param distributor The created senders will be tied to this 
   * market data distributor
   * @return Not null
   */
  Collection<MarketDataSender> create(MarketDataDistributor distributor);

}
