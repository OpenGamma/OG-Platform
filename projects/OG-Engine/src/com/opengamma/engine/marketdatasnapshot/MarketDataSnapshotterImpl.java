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
 * Default implementation of {@link MarketDataSnapshotter}.
 */
public class MarketDataSnapshotterImpl implements MarketDataSnapshotter {

  @Override
  public StructuredMarketDataSnapshot createSnapshot(ViewClient client, ViewCycle cycle) {
    return null;
  }

}
