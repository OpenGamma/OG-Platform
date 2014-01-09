/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import java.util.List;

import com.opengamma.master.ChangeProvidingCombinedMaster;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMetaDataRequest;
import com.opengamma.master.config.ConfigMetaDataResult;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;

/**
 * A {@link ConfigMaster} which delegates its calls to a list of underlying {@link ConfigMaster}s.
 * 
 * This class extends {@link ChangeProvidingCombinedMaster} to implement methods specific to the {@link HistoricalTimeSeriesMaster}.
 */
public class CombinedConfigMaster extends ChangeProvidingCombinedMaster<ConfigDocument, ConfigMaster> implements ConfigMaster {

  public CombinedConfigMaster(List<ConfigMaster> masterList) {
    super(masterList);
  }

  //interfaces defined for purposes of brevity in search() and history() implementations
  private interface ConfigMasterSearchStrategy<R> extends SearchStrategy<ConfigDocument, ConfigMaster, ConfigSearchRequest<R>> { }
  
  private interface ConfigMasterHistoryStrategy<R> extends SearchStrategy<ConfigDocument, ConfigMaster, ConfigHistoryRequest<R>> { }
  
  
  @Override
  public <R> ConfigSearchResult<R> search(ConfigSearchRequest<R> request) {
    final ConfigSearchResult<R> result = new ConfigSearchResult<R>();
    
    ConfigMasterSearchStrategy<R> searchStrategy = new ConfigMasterSearchStrategy<R>() {

      @Override
      public ConfigSearchResult<R> search(ConfigMaster master, ConfigSearchRequest<R> searchRequest) {
        ConfigSearchResult<R> searchResult = master.search(searchRequest);
        result.setVersionCorrection(searchResult.getVersionCorrection());
        return searchResult;
      }
    };
    
    pagedSearch(searchStrategy, result, request);
    
    return result;
  }

  @Override
  public <R> ConfigHistoryResult<R> history(ConfigHistoryRequest<R> request) {
    final ConfigHistoryResult<R> result = new ConfigHistoryResult<R>();
    
    ConfigMasterHistoryStrategy<R> searchStrategy = new ConfigMasterHistoryStrategy<R>() {

      @Override
      public ConfigHistoryResult<R> search(ConfigMaster master, ConfigHistoryRequest<R> searchRequest) {
        ConfigHistoryResult<R> searchResult = master.history(searchRequest);
        return searchResult;
      }
    };
    
    pagedSearch(searchStrategy, result, request);
    
    return result;

  }

  @Override
  public ConfigMetaDataResult metaData(ConfigMetaDataRequest request) {
    ConfigMetaDataResult result = new ConfigMetaDataResult();
    for (ConfigMaster master : getMasterList()) {
      ConfigMetaDataResult masterResult = master.metaData(request);
      result.getConfigTypes().addAll(masterResult.getConfigTypes());
    }
    return result;
  }

}
