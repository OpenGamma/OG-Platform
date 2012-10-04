/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.client;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.master.marketdatasnapshot.*;

/**
 * A {@link MarketDataSnapshotMaster} that combines the behavior of the masters
 * in the session, user and global contexts. 
 */
public class CombinedMarketDataSnapshotMaster extends CombinedMaster<ManageableMarketDataSnapshot, MarketDataSnapshotDocument, MarketDataSnapshotMaster> implements MarketDataSnapshotMaster {

  /* package */CombinedMarketDataSnapshotMaster(final CombiningMaster<ManageableMarketDataSnapshot, MarketDataSnapshotDocument, MarketDataSnapshotMaster, ?> combining,
      final MarketDataSnapshotMaster sessionMaster, final MarketDataSnapshotMaster userMaster, final MarketDataSnapshotMaster globalMaster) {
    super(combining, sessionMaster, userMaster, globalMaster);
  }

  @Override
  public ChangeManager changeManager() {
    // TODO: if needed
    throw new UnsupportedOperationException();
  }

  @Override
  public MarketDataSnapshotSearchResult search(final MarketDataSnapshotSearchRequest request) {
    final MarketDataSnapshotSearchResult result = new MarketDataSnapshotSearchResult();
    if (getSessionMaster() != null) {
      result.getDocuments().addAll(getSessionMaster().search(request).getDocuments());
    }
    if (getUserMaster() != null) {
      result.getDocuments().addAll(getUserMaster().search(request).getDocuments());
    }
    if (getGlobalMaster() != null) {
      result.getDocuments().addAll(getGlobalMaster().search(request).getDocuments());
    }
    return result;
  }

  @Override
  public Map<UniqueId, MarketDataSnapshotDocument> get(Collection<UniqueId> uniqueIds) {
    Map<UniqueId, MarketDataSnapshotDocument> map = newHashMap();
    for (UniqueId uniqueId : uniqueIds) {
      map.put(uniqueId, get(uniqueId));      
    }
    return map;
  }

  /**
   * Callback interface for the search operation to sort, filter and process results.
   */
  public interface SearchCallback extends CombinedMaster.SearchCallback<MarketDataSnapshotDocument> {
  }

  public void search(final MarketDataSnapshotSearchRequest request, final SearchCallback callback) {
    // TODO: parallel operation of any search requests
    final MarketDataSnapshotSearchResult sessionResult = (getSessionMaster() != null) ? getSessionMaster().search(request) : null;
    final MarketDataSnapshotSearchResult userResult = (getUserMaster() != null) ? getUserMaster().search(request) : null;
    final MarketDataSnapshotSearchResult globalResult = (getGlobalMaster() != null) ? getGlobalMaster().search(request) : null;
    search(sessionResult, userResult, globalResult, callback);
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
