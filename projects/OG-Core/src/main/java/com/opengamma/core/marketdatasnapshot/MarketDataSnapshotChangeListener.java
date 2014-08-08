/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import com.opengamma.core.ObjectChangeListener;
import com.opengamma.id.ObjectId;

/**
 * Listener providing callbacks when the market data snapshot changes.
 */
public interface MarketDataSnapshotChangeListener extends ObjectChangeListener {

  /**
   * Callback that is invoked if the object identifier now refers to a different snapshot.
   * 
   * @param oid the object identifier, not null
   */
  @Override
  void objectChanged(ObjectId oid);

}
