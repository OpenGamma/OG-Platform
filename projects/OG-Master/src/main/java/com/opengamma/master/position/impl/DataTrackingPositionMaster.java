/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractDataTrackingMaster;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;

/**
 * Position master which tracks accesses using UniqueIds.
 */
public class DataTrackingPositionMaster extends AbstractDataTrackingMaster<PositionDocument, PositionMaster> implements PositionMaster {
  
  public DataTrackingPositionMaster(PositionMaster delegate) {
    super(delegate);
  }

  @Override
  public PositionSearchResult search(PositionSearchRequest request) {
    PositionSearchResult searchResult = delegate().search(request);
    trackDocs(searchResult.getDocuments());
    return searchResult;
  }

  @Override
  public PositionHistoryResult history(PositionHistoryRequest request) {
    PositionHistoryResult historyResult = delegate().history(request);
    trackDocs(historyResult.getDocuments());
    return historyResult;
  }

  @Override
  public ManageableTrade getTrade(UniqueId tradeId) {
    //trades are wrapped by positions so don't need to
    //be tracked
    return delegate().getTrade(tradeId);
  }

  
  
}
