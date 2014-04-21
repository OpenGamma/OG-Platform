/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractDocumentsResult;
import com.opengamma.master.ChangeProvidingCombinedMaster;
import com.opengamma.master.CombinedMaster;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;

/**
 * A {@link PositionMaster} which delegates its calls to a list of underlying {@link PositionMaster}s.
 * 
 * This class extends {@link ChangeProvidingCombinedMaster} to implement methods specific to the {@link PositionMaster}.
 */
public class CombinedPositionMaster extends ChangeProvidingCombinedMaster<PositionDocument, PositionMaster> implements PositionMaster {

  public CombinedPositionMaster(List<PositionMaster> masters) {
    super(masters);
  }

  @Override
  public PositionSearchResult search(final PositionSearchRequest overallRequest) {
    final PositionSearchResult overallResult = new PositionSearchResult();
    
    pagedSearch(new PositionSearchStrategy() {
      
      @Override
      public AbstractDocumentsResult<PositionDocument> search(PositionMaster master, PositionSearchRequest searchRequest) {
        PositionSearchResult masterResult = master.search(searchRequest);
        overallResult.setVersionCorrection(masterResult.getVersionCorrection());
        return masterResult;
      }
    }, overallResult, overallRequest);
    
    
    return overallResult;
  }

  /**
   * Callback interface for position searches
   */
  private interface PositionSearchStrategy extends SearchStrategy<PositionDocument, PositionMaster, PositionSearchRequest> { }

  
  /**
   * Callback interface for the search operation to sort, filter and process results.
   */
  public interface SearchCallback extends CombinedMaster.SearchCallback<PositionDocument, PositionMaster> {
  }

  public void search(final PositionSearchRequest request, final SearchCallback callback) {
    // TODO: parallel operation of any search requests
    List<PositionSearchResult> results = Lists.newArrayList();
    for (PositionMaster master : getMasterList()) {
      results.add(master.search(request));
    }
    search(results, callback);
  }

  @Override
  public PositionHistoryResult history(final PositionHistoryRequest request) {
    final PositionMaster master = getMasterByScheme(request.getObjectId().getScheme());
    if (master != null) {
      return master.history(request);
    }
    return (new Try<PositionHistoryResult>() {
      @Override
      public PositionHistoryResult tryMaster(final PositionMaster master) {
        return master.history(request);
      }
    }).each(request.getObjectId().getScheme());
  }

  @Override
  public ManageableTrade getTrade(UniqueId tradeId) {
    return null;
  }

}
