/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.master.AbstractDocumentsResult;
import com.opengamma.master.ChangeProvidingCombinedMaster;
import com.opengamma.master.CombinedMaster;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecurityMetaDataRequest;
import com.opengamma.master.security.SecurityMetaDataResult;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;

/**
 * A {@link SecurityMaster} which delegates its calls to a list of underlying {@link SecurityMaster}s.
 * 
 * This class extends {@link ChangeProvidingCombinedMaster} to implement methods specific to the {@link SecurityMaster}.
 */
public class CombinedSecurityMaster extends ChangeProvidingCombinedMaster<SecurityDocument, SecurityMaster> implements SecurityMaster {

  public CombinedSecurityMaster(final List<SecurityMaster> masters) {
    super(masters);
  }

  @Override
  public SecurityMetaDataResult metaData(final SecurityMetaDataRequest request) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SecuritySearchResult search(final SecuritySearchRequest overallRequest) {
    final SecuritySearchResult overallResult = new SecuritySearchResult();
    pagedSearch(new SecuritySearchStrategy() {
      
      @Override
      public AbstractDocumentsResult<SecurityDocument> search(SecurityMaster master, SecuritySearchRequest searchRequest) {
        SecuritySearchResult masterResult = master.search(searchRequest);
        overallResult.setVersionCorrection(masterResult.getVersionCorrection());
        return masterResult;
      }
    }, overallResult, overallRequest);
    

    return overallResult;
  }

  /**
   * Callback interface for security searches
   */
  private interface SecuritySearchStrategy extends SearchStrategy<SecurityDocument, SecurityMaster, SecuritySearchRequest> { }

  
  
  /**
   * Callback interface for the search operation to sort, filter and process results.
   */
  public interface SearchCallback extends CombinedMaster.SearchCallback<SecurityDocument, SecurityMaster> {
  }

  public void search(final SecuritySearchRequest request, final SearchCallback callback) {
    // TODO: parallel operation of any search requests
    List<SecuritySearchResult> results = Lists.newArrayList();
    for (SecurityMaster master : getMasterList()) {
      results.add(master.search(request));
    }
    search(results, callback);
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
