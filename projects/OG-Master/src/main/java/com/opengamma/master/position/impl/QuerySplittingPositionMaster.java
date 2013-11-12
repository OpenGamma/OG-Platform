/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.impl.AbstractQuerySplittingMaster;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;

/**
 * A {@link PositionMaster} implementation that divides search operations into a number of smaller operations to pass to the underlying. This is intended for use with some database backed position
 * masters where performance decreases, or becomes unstable, with large queries.
 */
public class QuerySplittingPositionMaster extends AbstractQuerySplittingMaster<PositionDocument, PositionMaster> implements PositionMaster {

  /**
   * The maximum size of request to pass to {@link PositionMaster#search}, zero or negative for no limit.
   */
  private int _maxSearchRequest;

  /**
   * Creates a new instance wrapping the underlying with default properties.
   * 
   * @param underlying the underlying position master to satisfy the requests, not null
   */
  public QuerySplittingPositionMaster(final PositionMaster underlying) {
    super(underlying);
  }

  /**
   * Returns the maximum number of items to pass to the {@link PositionMaster#search} method in each call.
   * 
   * @return the current limit, zero or negative if none
   */
  public int getMaxSearchRequest() {
    return _maxSearchRequest;
  }

  /**
   * Sets the maximum number of items to pass to the {@link PositionMaster#search} method in each call.
   * 
   * @param maxSearchRequest the new limit, zero or negative if none
   */
  public void setMaxSearchRequest(final int maxSearchRequest) {
    _maxSearchRequest = maxSearchRequest;
  }

  protected Collection<PositionSearchRequest> splitSearchRequest(final PositionSearchRequest request) {
    if (request.getPositionObjectIds() == null) {
      // Can only split requests with multiple object ids
      return null;
    }
    if (!PagingRequest.ALL.equals(request.getPagingRequest()) && !PagingRequest.NONE.equals(request.getPagingRequest())) {
      // Can only split requests with no paging
      return null;
    }
    int chunkSize = getMaxSearchRequest();
    final int count = request.getPositionObjectIds().size();
    if ((chunkSize <= 0) || (chunkSize >= count)) {
      // Request too small, or splitting is disabled
      return null;
    }
    int chunks = (count + chunkSize - 1) / chunkSize;
    final Collection<PositionSearchRequest> requests = new ArrayList<PositionSearchRequest>();
    final Iterator<ObjectId> positions = request.getPositionObjectIds().iterator();
    for (int i = 0; i < count;) {
      chunkSize = (count - i) / (chunks--);
      final PositionSearchRequest subRequest = request.clone();
      subRequest.getPositionObjectIds().clear();
      for (int j = 0; (j < chunkSize) && positions.hasNext(); j++) {
        subRequest.addPositionObjectId(positions.next());
      }
      requests.add(subRequest);
      i += chunkSize;
    }
    return requests;
  }

  protected void mergeSplitSearchResult(final PositionSearchResult mergeWith, final PositionSearchResult result) {
    final Collection<PositionDocument> documents = result.getDocuments();
    mergeWith.getDocuments().addAll(documents);
    mergeWith.setPaging(Paging.of(PagingRequest.ALL, ((mergeWith.getPaging() != null) ? mergeWith.getPaging().getTotalItems() : 0) + documents.size()));
    mergeWith.setVersionCorrection(result.getVersionCorrection());
  }

  protected PositionSearchResult callSplitSearchRequest(final Collection<PositionSearchRequest> requests) {
    final PositionSearchResult result = new PositionSearchResult();
    for (PositionSearchRequest request : requests) {
      mergeSplitSearchResult(result, getUnderlying().search(request));
    }
    return result;
  }

  // PositionMaster

  /**
   * When splitting is enabled, and the request is for more positions than the split size, two or more requests are made to the underlying master. {@inheritDoc}
   */
  @Override
  public PositionSearchResult search(final PositionSearchRequest request) {
    if (canSplit()) {
      final Collection<PositionSearchRequest> requests = splitSearchRequest(request);
      if (requests == null) {
        // Small query pass-through
        return getUnderlying().search(request);
      } else {
        // Multiple queries
        return callSplitSearchRequest(requests);
      }
    } else {
      // Splitting disabled
      return getUnderlying().search(request);
    }
  }

  @Override
  public PositionHistoryResult history(final PositionHistoryRequest request) {
    return getUnderlying().history(request);
  }

  @Override
  public ManageableTrade getTrade(final UniqueId tradeId) {
    return getUnderlying().getTrade(tradeId);
  }
}
