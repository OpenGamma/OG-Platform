/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.batch.rest;


import com.opengamma.batch.domain.MarketData;
import com.opengamma.batch.domain.MarketDataValue;
import com.opengamma.batch.domain.RiskRun;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.impl.AbstractRemoteMaster;
import com.opengamma.util.ArgumentChecker;

import java.net.URI;
import java.util.Set;

/**
 * Provides access to a remote {@link com.opengamma.financial.batch.BatchMaster}.
 */
public class RemoteBatchMaster extends AbstractRemoteMaster {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteBatchMaster(final URI baseUri) {
    super(baseUri);
  }

  public BatchRunSearchResult searchBatchRun(BatchRunSearchRequest requestRun) {
    ArgumentChecker.notNull(requestRun, "requestRun");        
    String msgBase64 = getRestClient().encodeBean(requestRun);
    URI uri = BatchRunResource.uri(getBaseUri(), msgBase64);
    return accessRemote(uri).get(BatchRunSearchResult.class);
  }

  public RiskRun getBatchRun(ObjectId batchUniqueId) {
    ArgumentChecker.notNull(batchUniqueId, "batchUniqueId");        
    URI uri = BatchRunResource.uri(getBaseUri(), batchUniqueId);
    return accessRemote(uri).get(RiskRun.class);
  }

  public void deleteBatchRun(ObjectId batchUniqueId) {
    ArgumentChecker.notNull(batchUniqueId, "batchUniqueId");        
    URI uri = BatchRunResource.uri(getBaseUri(), batchUniqueId);
    accessRemote(uri).delete();
  }

  public UniqueId createMarketData(UniqueId MarketDataUid) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public void addValuesToSnapshot(UniqueId marketDataSnapshotUniqueId, Set<MarketDataValue> values) {
    //To change body of implemented methods use File | Settings | File Templates.
  }
  
  public MarketData getSnapshot(ObjectId marketDataId) {
    ArgumentChecker.notNull(marketDataId, "marketDataId");        
    URI uri = MarketDataResource.uri(getBaseUri(), marketDataId);
    return accessRemote(uri).get(MarketData.class);
  }
}
