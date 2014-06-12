/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange.impl;

import com.opengamma.master.AbstractDataTrackingMaster;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeHistoryRequest;
import com.opengamma.master.exchange.ExchangeHistoryResult;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;

/**
 * Exchange master which tracks accesses using UniqueIds.
 */
public class DataTrackingExchangeMaster extends AbstractDataTrackingMaster<ExchangeDocument, ExchangeMaster> implements ExchangeMaster {
  
  public DataTrackingExchangeMaster(ExchangeMaster delegate) {
    super(delegate);
  }

  @Override
  public ExchangeSearchResult search(ExchangeSearchRequest request) {
    ExchangeSearchResult searchResult = delegate().search(request);
    trackDocs(searchResult.getDocuments());
    return searchResult;
  }

  @Override
  public ExchangeHistoryResult history(ExchangeHistoryRequest request) {
    ExchangeHistoryResult historyResult = delegate().history(request);
    trackDocs(historyResult.getDocuments());
    return historyResult;
  }

  
}
