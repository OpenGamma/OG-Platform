/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.distribution;

import java.util.Collection;
import java.util.Collections;

/**
 * 
 *
 * @author pietari
 */
public class EmptyMarketDataSenderFactory implements MarketDataSenderFactory {

  @Override
  public Collection<MarketDataSender> create(MarketDataDistributor distributor) {
    return Collections.emptyList();
  }

}
