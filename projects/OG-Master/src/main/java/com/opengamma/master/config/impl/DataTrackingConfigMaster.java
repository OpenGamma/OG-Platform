/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import com.opengamma.master.AbstractDataTrackingMaster;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMetaDataRequest;
import com.opengamma.master.config.ConfigMetaDataResult;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;

/**
 * Config master which tracks accesses using UniqueIds.
 */
public class DataTrackingConfigMaster extends AbstractDataTrackingMaster<ConfigDocument, ConfigMaster> implements ConfigMaster {
  
  public DataTrackingConfigMaster(ConfigMaster delegate) {
    super(delegate);
  }

  @Override
  public <R> ConfigSearchResult<R> search(ConfigSearchRequest<R> request) {
    ConfigSearchResult<R> searchResult = delegate().search(request);
    trackDocs(searchResult.getDocuments());
    return searchResult;
  }

  @Override
  public <R> ConfigHistoryResult<R> history(ConfigHistoryRequest<R> request) {
    ConfigHistoryResult<R> historyResult = delegate().history(request);
    trackDocs(historyResult.getDocuments());
    return historyResult;
  }

  @Override
  public ConfigMetaDataResult metaData(ConfigMetaDataRequest request) {
    return delegate().metaData(request);
  }
  
  
}
