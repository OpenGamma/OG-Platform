/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdatasnapshot;

import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.client.ViewClient;

/**
 * This service handles the relationship between running {@link ViewClient}s and {@link StructuredMarketDataSnapshot}s.
 * 
 */
public interface MarketDataSnapshotter {

  //TODO: create snapshot from latest cycle
  
  /**
   * Produces a snapshot from a view cycle
   * @param client The client to use
   * @param cycle The cycle on which to base the snapshot
   * @return A snapshot representing the data used in the given view cycle
   */
  StructuredMarketDataSnapshot createSnapshot(ViewClient client, ViewCycle cycle);
}
