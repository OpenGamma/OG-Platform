/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.client;

import java.util.List;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.*;

/**
 * A {@link SecurityMaster} that combines the behavior of the masters
 * in the session, user and global contexts. 
 */
public class CombinedSecurityMaster extends CombinedMaster<SecurityDocument, SecurityMaster> implements SecurityMaster {

  /* package */CombinedSecurityMaster(final CombiningMaster<SecurityDocument, SecurityMaster, ?> combining, final SecurityMaster sessionMaster, final SecurityMaster userMaster,
      final SecurityMaster globalMaster) {
    super(combining, sessionMaster, userMaster, globalMaster);
  }

  @Override
  public ChangeManager changeManager() {
    throw new UnsupportedOperationException();
  }

  @Override
  public SecurityMetaDataResult metaData(final SecurityMetaDataRequest request) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SecuritySearchResult search(final SecuritySearchRequest request) {
    final SecuritySearchResult result = new SecuritySearchResult();
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

  /**
   * Callback interface for the search operation to sort, filter and process results.
   */
  public interface SearchCallback extends CombinedMaster.SearchCallback<SecurityDocument> {
  }

  public void search(final SecuritySearchRequest request, final SearchCallback callback) {
    // TODO: parallel operation of any search requests
    final SecuritySearchResult sessionResult = (getSessionMaster() != null) ? getSessionMaster().search(request) : null;
    final SecuritySearchResult userResult = (getUserMaster() != null) ? getUserMaster().search(request) : null;
    final SecuritySearchResult globalResult = (getGlobalMaster() != null) ? getGlobalMaster().search(request) : null;
    search(sessionResult, userResult, globalResult, callback);
  }

  @Override
  public SecurityHistoryResult history(final SecurityHistoryRequest request) {
    final SecurityMaster master = getMasterByScheme(request.getObjectId().getScheme());
    if (master != null) {
      return master.history(request);
    }
    return (new Try<SecurityHistoryResult>() {
      @Override
      public SecurityHistoryResult tryMaster(final SecurityMaster master) {
        return master.history(request);
      }
    }).each(request.getObjectId().getScheme());
  }

}
