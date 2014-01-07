/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot.impl;

import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.master.AbstractDocumentsResult;
import com.opengamma.master.ChangeProvidingCombinedMaster;
import com.opengamma.master.CombinedMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotHistoryResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;

/**
 * A {@link MarketDataSnapshotMaster} which delegates its calls to a list of underlying {@link MarketDataSnapshotMaster}s.
 * 
 * This class extends {@link ChangeProvidingCombinedMaster} to implement methods specific to the {@link MarketDataSnapshotMaster}.
 */
public class CombinedMarketDataSnapshotMaster extends ChangeProvidingCombinedMaster<MarketDataSnapshotDocument, MarketDataSnapshotMaster> implements MarketDataSnapshotMaster {

  public CombinedMarketDataSnapshotMaster(final List<MarketDataSnapshotMaster> masterList) {
    super(masterList);
  }

  @Override
  public MarketDataSnapshotSearchResult search(MarketDataSnapshotSearchRequest overallRequest) {
    final MarketDataSnapshotSearchResult overallResult = new MarketDataSnapshotSearchResult();
    
    pagedSearch(new SnapshotSearchStrategy() {

      @Override
      public AbstractDocumentsResult<MarketDataSnapshotDocument> search(MarketDataSnapshotMaster master, MarketDataSnapshotSearchRequest searchRequest) {
        MarketDataSnapshotSearchResult masterResult = master.search(searchRequest);
        masterResult.setVersionCorrection(overallResult.getVersionCorrection());
        return masterResult;
      }
    }, overallResult, overallRequest);
    
    return overallResult;
  }
  
  /**
   * Callback interface for snapshot searches
   */
  private interface SnapshotSearchStrategy extends SearchStrategy<MarketDataSnapshotDocument, MarketDataSnapshotMaster, MarketDataSnapshotSearchRequest> { }

  /**
   * Callback interface for the search operation to sort, filter and process results.
   */
  public interface SearchCallback extends CombinedMaster.SearchCallback<MarketDataSnapshotDocument, MarketDataSnapshotMaster> {
  }

  public void search(final MarketDataSnapshotSearchRequest request, final SearchCallback callback) {
    // TODO: parallel operation of any search requests
    List<MarketDataSnapshotSearchResult> results = Lists.newArrayList();
    for (MarketDataSnapshotMaster master : getMasterList()) {
      results.add(master.search(request));
    }
    search(results, callback);
  }

  @Override
  public MarketDataSnapshotHistoryResult history(final MarketDataSnapshotHistoryRequest request) {
    final MarketDataSnapshotMaster master = getMasterByScheme(request.getObjectId().getScheme());
    if (master != null) {
      return master.history(request);
    }
    return (new Try<MarketDataSnapshotHistoryResult>() {
      @Override
      public MarketDataSnapshotHistoryResult tryMaster(final MarketDataSnapshotMaster master) {
        return master.history(request);
      }
    }).each(request.getObjectId().getScheme());
  }  

}
