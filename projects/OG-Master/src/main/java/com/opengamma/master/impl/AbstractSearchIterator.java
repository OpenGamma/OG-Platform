/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.master.AbstractDocument;
import com.opengamma.master.AbstractMaster;
import com.opengamma.master.AbstractSearchRequest;
import com.opengamma.master.AbstractSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.PagingRequest;

/**
 * An iterator that searches a config master as an iterator.
 * <p>
 * Large systems may store a large amount of data in each master.
 * A simple search request that pulls back the entire database is unrealistic.
 * This remote iterator allows the database to be queried in a consistent way remotely.
 * 
 * @param <D>  the type of the document
 * @param <M>  the type of the master
 * @param <R>  the type of the search request
 */
public abstract class AbstractSearchIterator<D extends AbstractDocument, M extends AbstractMaster<D>, R extends AbstractSearchRequest>
    implements Iterator<D> {

  /**
   * The master that is being used.
   */
  private M _master;
  /**
   * The request object that is being used.
   */
  private final R _request;
  /**
   * The last result object.
   */
  private AbstractSearchResult<D> _currentBatch;
  /**
   * The index of the next object within the batch result.
   */
  private int _currentBatchIndex;
  /**
   * The current document, null if not fetched, at end or removed.
   */
  private D _current;
  /**
   * The overall index of the last retrieved object.
   */
  private int _overallIndex;

  /**
   * Creates an instance based on a request.
   * <p>
   * The request will be altered during the iteration.
   * 
   * @param master  the underlying master, not null
   * @param request  the request object, not null
   */
  protected AbstractSearchIterator(M master, R request) {
    ArgumentChecker.notNull(master, "master");
    ArgumentChecker.notNull(request, "request");
    _master = master;
    _request = request;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean hasNext() {
    if (_currentBatch == null || _currentBatchIndex >= _currentBatch.getDocuments().size()) {
      doFetch();
    }
    return (_currentBatch != null && _currentBatchIndex < _currentBatch.getDocuments().size());
  }

  @Override
  public D next() {
    if (hasNext() == false) {
      throw new NoSuchElementException("No more elements found");
    }
    return doNext();
  }

  /**
   * Removes the last seen document.
   */
  @Override
  public void remove() {
    if (_current == null) {
      throw new IllegalStateException();
    }
  }

  /**
   * Gets the overall index of the next entry.
   * <p>
   * This number may skip if a bad entry is found.
   * 
   * @return the overall index of the next entry, 0 if next() not called yet
   */
  public int nextIndex() {
    return _overallIndex;
  }

  private void doFetch() {
    try {
      // try to fetch a batch of 20 documents
      _request.setPagingRequest(PagingRequest.ofIndex(_overallIndex, 20));
      _currentBatch = doSearch(_request);
      
    } catch (RuntimeException ex) {
      doFetchOne(ex);
    }
    
    // ensure same vc for whole iterator
    _request.setVersionCorrection(_currentBatch.getVersionCorrection());
    
    // check results
    if (_currentBatch.getPaging().getFirstItem() < _overallIndex) {
      _currentBatchIndex = (_overallIndex - _currentBatch.getPaging().getFirstItem());
    } else {
      _currentBatchIndex = 0;
    }
  }

  /**
   * Fetches the next one document.
   * 
   * @param ex  the original exception, not null
   */
  private void doFetchOne(RuntimeException ex) {
    // try to load just the next document
    int maxFailures = 5;
    if (_currentBatch != null) {
      maxFailures = _currentBatch.getPaging().getTotalItems() - _overallIndex;  // if we have results, use maximum count
      maxFailures = Math.min(maxFailures, 20);
    }
    while (maxFailures > 0) {
      try {
        _request.setPagingRequest(PagingRequest.ofIndex(_overallIndex, 1));
        _currentBatch = doSearch(_request);
        return;
        
      } catch (RuntimeException ex2) {
        _overallIndex++;  // abandon this document
        maxFailures--;
      }
    }
    throw new OpenGammaRuntimeException("Multiple documents failed to load", ex);
  }

  private D doNext() {
    _current = _currentBatch.getDocuments().get(_currentBatchIndex);
    _currentBatchIndex++;
    _overallIndex++;
    return _current;
  }

  /**
   * Performs the search on the master.
   * 
   * @param request  the request to send, not null
   * @return the search result, not null
   * @throws RuntimeException if an error occurs
   */
  protected abstract AbstractSearchResult<D> doSearch(R request);

  //-----------------------------------------------------------------------
  /**
   * Gets the underlying master.
   * 
   * @return the master, not null
   */
  public M getMaster() {
    return _master;
  }

  /**
   * Gets the request object that is being used.
   * 
   * @return the request, not null
   */
  public R getRequest() {
    return _request;
  }

}
