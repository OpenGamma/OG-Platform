/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.client;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.Map;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;

/**
 * A {@link PositionMaster} that combines the behavior of the masters
 * in the session, user and global contexts. 
 */
public class CombinedPositionMaster extends CombinedMaster<PositionDocument, PositionMaster> implements PositionMaster {

  /* package */CombinedPositionMaster(final CombiningMaster<PositionDocument, PositionMaster, ?> combining, final PositionMaster sessionMaster, final PositionMaster userMaster,
      final PositionMaster globalMaster) {
    super(combining, sessionMaster, userMaster, globalMaster);
  }

  @Override
  public ChangeManager changeManager() {
    throw new UnsupportedOperationException();
  }

  @Override
  public PositionSearchResult search(final PositionSearchRequest request) {
    final PositionSearchResult result = new PositionSearchResult();
    if (getSessionMaster() != null) {
      PositionSearchResult search = getSessionMaster().search(request);
      result.getDocuments().addAll(search.getDocuments());
      result.setVersionCorrection(search.getVersionCorrection());
    }
    if (getUserMaster() != null) {
      PositionSearchResult search = getUserMaster().search(request);
      result.getDocuments().addAll(search.getDocuments());
      result.setVersionCorrection(search.getVersionCorrection());
    }
    if (getGlobalMaster() != null) {
      PositionSearchResult search = getGlobalMaster().search(request);
      result.getDocuments().addAll(search.getDocuments());
      result.setVersionCorrection(search.getVersionCorrection());
    }
    return result;
  }

  /**
   * Callback interface for the search operation to sort, filter and process results.
   */
  public interface SearchCallback extends CombinedMaster.SearchCallback<PositionDocument> {
  }

  public void search(final PositionSearchRequest request, final SearchCallback callback) {
    // TODO: parallel operation of any search requests
    final PositionSearchResult sessionResult = (getSessionMaster() != null) ? getSessionMaster().search(request) : null;
    final PositionSearchResult userResult = (getUserMaster() != null) ? getUserMaster().search(request) : null;
    final PositionSearchResult globalResult = (getGlobalMaster() != null) ? getGlobalMaster().search(request) : null;
    search(sessionResult, userResult, globalResult, callback);
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

  @Override
  public Map<UniqueId, PositionDocument> get(Collection<UniqueId> uniqueIds) {
    Map<UniqueId, PositionDocument> map = newHashMap();
    for (UniqueId uniqueId : uniqueIds) {
      map.put(uniqueId, get(uniqueId));
    }
    return map;
  }
}
