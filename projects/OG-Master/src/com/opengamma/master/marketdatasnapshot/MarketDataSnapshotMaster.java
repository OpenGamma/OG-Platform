/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot;

import com.opengamma.master.AbstractMaster;
import com.opengamma.master.listener.NotifyingMaster;
import com.opengamma.util.PublicSPI;

/**
 * A general-purpose market data snapshot master.
 * <p>
 */
@PublicSPI
public interface MarketDataSnapshotMaster extends AbstractMaster<MarketDataSnapshotDocument>,  NotifyingMaster {

  /**
   * Searches for snasphots matching the specified search criteria.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  MarketDataSnapshotSearchResult search(MarketDataSnapshotSearchRequest request);

  /**
   * Queries the history of a single snapshot.
   * <p>
   * The request must contain an object identifier to identify the snapshot.
   * 
   * @param request  the history request, not null
   * @return the snapshot history, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  MarketDataSnapshotHistoryResult history(MarketDataSnapshotHistoryRequest request);

}
