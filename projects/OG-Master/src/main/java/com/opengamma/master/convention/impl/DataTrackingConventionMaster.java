/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.convention.impl;

import com.opengamma.master.AbstractDataTrackingMaster;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionHistoryRequest;
import com.opengamma.master.convention.ConventionHistoryResult;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ConventionMetaDataRequest;
import com.opengamma.master.convention.ConventionMetaDataResult;
import com.opengamma.master.convention.ConventionSearchRequest;
import com.opengamma.master.convention.ConventionSearchResult;

/**
 * Convention master which tracks accesses using UniqueIds.
 */
public class DataTrackingConventionMaster extends AbstractDataTrackingMaster<ConventionDocument, ConventionMaster> implements ConventionMaster {

  public DataTrackingConventionMaster(ConventionMaster delegate) {
    super(delegate);
  }

  @Override
  public ConventionSearchResult search(ConventionSearchRequest request) {
    ConventionSearchResult searchResult = delegate().search(request);
    trackDocs(searchResult.getDocuments());
    return searchResult;
  }

  @Override
  public ConventionHistoryResult history(ConventionHistoryRequest request) {
    ConventionHistoryResult historyResult = delegate().history(request);
    trackDocs(historyResult.getDocuments());
    return historyResult;
  }

  
  @Override
  public ConventionMetaDataResult metaData(ConventionMetaDataRequest request) {
    return delegate().metaData(request);
  }
}
