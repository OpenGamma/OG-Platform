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
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioHistoryRequest;
import com.opengamma.master.portfolio.PortfolioHistoryResult;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;

/**
 * A {@link PortfolioMaster} that combines the behavior of the masters
 * in the session, user and global contexts. 
 */
public class CombinedPortfolioMaster extends CombinedMaster<PortfolioDocument, PortfolioMaster> implements PortfolioMaster {

  /* package */CombinedPortfolioMaster(final CombiningMaster<PortfolioDocument, PortfolioMaster, ?> combining, final PortfolioMaster sessionMaster, final PortfolioMaster userMaster,
      final PortfolioMaster globalMaster) {
    super(combining, sessionMaster, userMaster, globalMaster);
  }

  @Override
  public ChangeManager changeManager() {
    throw new UnsupportedOperationException();
  }

  @Override
  public PortfolioSearchResult search(final PortfolioSearchRequest request) {
    final PortfolioSearchResult result = new PortfolioSearchResult();
    if (getSessionMaster() != null) {
      PortfolioSearchResult search = getSessionMaster().search(request);
      result.getDocuments().addAll(search.getDocuments());
      result.setVersionCorrection(search.getVersionCorrection());
    }
    if (getUserMaster() != null) {
      PortfolioSearchResult search = getUserMaster().search(request);
      result.getDocuments().addAll(search.getDocuments());
      result.setVersionCorrection(search.getVersionCorrection());
    }
    if (getGlobalMaster() != null) {
      PortfolioSearchResult search = getGlobalMaster().search(request);
      result.getDocuments().addAll(search.getDocuments());
      result.setVersionCorrection(search.getVersionCorrection());
    }
    return result;
  }

  /**
   * Callback interface for the search operation to sort, filter and process results.
   */
  public interface SearchCallback extends CombinedMaster.SearchCallback<PortfolioDocument> {
  }

  public void search(final PortfolioSearchRequest request, final SearchCallback callback) {
    // TODO: parallel operation of any search requests
    final PortfolioSearchResult sessionResult = (getSessionMaster() != null) ? getSessionMaster().search(request) : null;
    final PortfolioSearchResult userResult = (getUserMaster() != null) ? getUserMaster().search(request) : null;
    final PortfolioSearchResult globalResult = (getGlobalMaster() != null) ? getGlobalMaster().search(request) : null;
    search(sessionResult, userResult, globalResult, callback);
  }

  @Override
  public PortfolioHistoryResult history(final PortfolioHistoryRequest request) {
    final PortfolioMaster master = getMasterByScheme(request.getObjectId().getScheme());
    if (master != null) {
      return master.history(request);
    }
    return (new Try<PortfolioHistoryResult>() {
      @Override
      public PortfolioHistoryResult tryMaster(final PortfolioMaster master) {
        return master.history(request);
      }
    }).each(request.getObjectId().getScheme());
  }

  @Override
  public ManageablePortfolioNode getNode(final UniqueId nodeId) {
    final PortfolioMaster master = getMasterByScheme(nodeId.getScheme());
    if (master != null) {
      return master.getNode(nodeId);
    }
    return (new Try<ManageablePortfolioNode>() {
      @Override
      public ManageablePortfolioNode tryMaster(final PortfolioMaster master) {
        return master.getNode(nodeId);
      }
    }).each(nodeId.getScheme());
  }

  @Override
  public Map<UniqueId, PortfolioDocument> get(Collection<UniqueId> uniqueIds) {
    Map<UniqueId, PortfolioDocument> map = newHashMap();
    for (UniqueId uniqueId : uniqueIds) {
      map.put(uniqueId, get(uniqueId));
    }
    return map;
  }
}
