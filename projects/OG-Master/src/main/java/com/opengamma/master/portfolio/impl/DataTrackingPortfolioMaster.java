/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.portfolio.impl;

import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractDataTrackingMaster;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioHistoryRequest;
import com.opengamma.master.portfolio.PortfolioHistoryResult;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;

/**
 * Portfolio master which tracks accesses using UniqueIds.
 */
public class DataTrackingPortfolioMaster extends AbstractDataTrackingMaster<PortfolioDocument, PortfolioMaster> implements PortfolioMaster {
  
  public DataTrackingPortfolioMaster(PortfolioMaster delegate) {
    super(delegate);
  }

  @Override
  public PortfolioSearchResult search(PortfolioSearchRequest request) {
    PortfolioSearchResult searchResult = delegate().search(request);
    trackDocs(searchResult.getDocuments());
    return searchResult;
  }

  @Override
  public PortfolioHistoryResult history(PortfolioHistoryRequest request) {
    PortfolioHistoryResult historyResult = delegate().history(request);
    trackDocs(historyResult.getDocuments());
    return historyResult;
  }

  @Override
  public ManageablePortfolioNode getNode(UniqueId nodeId) {
    ManageablePortfolioNode node = delegate().getNode(nodeId);
    trackId(node.getUniqueId());
    return node;
  }

  
}
