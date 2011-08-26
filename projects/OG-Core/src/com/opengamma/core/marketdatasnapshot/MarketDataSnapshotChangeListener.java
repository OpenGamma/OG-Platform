/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import com.opengamma.id.UniqueId;

/**
 * Listener providing callbacks when the market data snapshot changes.
 */
public interface MarketDataSnapshotChangeListener {

  /**
   * Callback that is invoked if the unique identifier now refers to a different snapshot.
   * 
   * @param uniqueId  the unique identifier, not null
   */
  void snapshotChanged(UniqueId uniqueId);

}
