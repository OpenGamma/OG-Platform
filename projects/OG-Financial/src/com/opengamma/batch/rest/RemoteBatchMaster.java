/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.batch.rest;


import com.opengamma.batch.BatchMaster;
import com.opengamma.batch.domain.MarketData;
import com.opengamma.batch.domain.MarketDataValue;
import com.opengamma.batch.domain.RiskRun;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.impl.AbstractRemoteMaster;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.master.security.impl.DataSecurityMasterResource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.tuple.Pair;

import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * Provides access to a remote {@link com.opengamma.financial.batch.BatchMaster}.
 */
public class RemoteBatchMaster extends AbstractRemoteMaster implements BatchMaster {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteBatchMaster(final URI baseUri) {
    super(baseUri);
  }


  @SuppressWarnings("unchecked")
  @Override
  public Pair<List<RiskRun>, Paging> searchRiskRun(BatchRunSearchRequest batchRunSearchRequest) {
    ArgumentChecker.notNull(batchRunSearchRequest, "batchRunSearchRequest");
    URI uri = DataBatchRunResource.uriSearch(getBaseUri());
    return accessRemote(uri).post(Pair.class, batchRunSearchRequest);
  }

  @Override
  public RiskRun getRiskRun(ObjectId batchId) {
    ArgumentChecker.notNull(batchId, "batchId");        
    URI uri = DataBatchRunResource.uri(getBaseUri(), batchId);
    return accessRemote(uri).get(RiskRun.class);
  }

  @Override
  public void deleteRiskRun(ObjectId batchId) {
    ArgumentChecker.notNull(batchId, "batchId");        
    URI uri = DataBatchRunResource.uri(getBaseUri(), batchId);
    accessRemote(uri).delete();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Pair<List<ViewResultEntry>, Paging> getBatchValues(ObjectId batchId, PagingRequest pagingRequest) {
    ArgumentChecker.notNull(batchId, "batchId");        
    URI uri = DataBatchRunResource.uriBatchValues(getBaseUri(), batchId);
    return accessRemote(uri).post(Pair.class, pagingRequest);
  }
  
  //////////////////////////////////


  @SuppressWarnings("unchecked")
  @Override
  public Pair<List<MarketData>, Paging> getMarketData(PagingRequest pagingRequest) {
    URI uri = DataMarketDataResource.uriMarketData(getBaseUri());
    return accessRemote(uri).post(Pair.class, pagingRequest);
  }

  @Override
  public MarketData getMarketDataById(ObjectId marketDataId) {
    URI uri = DataMarketDataResource.uriMarketData(getBaseUri(), marketDataId);
    return accessRemote(uri).get(MarketData.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Pair<List<MarketDataValue>, Paging> getMarketDataValues(ObjectId marketDataId, PagingRequest pagingRequest) {
    URI uri = DataMarketDataResource.uriMarketDataValues(getBaseUri(), marketDataId);
    return accessRemote(uri).post(Pair.class, pagingRequest);
  }
}
