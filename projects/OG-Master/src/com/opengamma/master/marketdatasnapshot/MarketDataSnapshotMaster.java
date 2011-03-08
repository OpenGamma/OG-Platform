/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot;

import com.opengamma.master.AbstractMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.PublicSPI;

/**
 * A general-purpose market data snapshot master.
 * <p>
 */
@PublicSPI
public interface MarketDataSnapshotMaster extends AbstractMaster<MarketDataSnapshotDocument> {

  /**
   * Searches for snasphots matching the specified search criteria.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  MarketDataSnapshotSearchResult search(MarketDataSnapshotSearchRequest request);
}
