/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.paging.PagingRequest;

/**
 * An iterator that searches a config master as an iterator.
 * <p>
 * Large systems may store a large amount of data in each master.
 * A simple search request that pulls back the entire database is unrealistic.
 * This remote iterator allows the database to be queried in a consistent way remotely.
 * 
 * @param <T>  the type of the configuration item
 */
@PublicSPI
public class ConfigMasterIterator<T> implements Iterator<ConfigItem<T>> {

  /**
   * The master that is being used.
   */
  private ConfigMaster _master;
  /**
   * The request object that is being used.
   */
  private final ConfigSearchRequest<T> _request;
  /**
   * The last result object.
   */
  private ConfigSearchResult<T> _currentBatch;
  /**
   * The index of the next object within the batch result.
   */
  private int _batchIndex;
  /**
   * The current document, null if not fetched, at end or removed.
   */
  private ConfigItem<T> _current;
  /**
   * The overall index of the last retrived object.
   */
  private int _overallIndex;

  /**
   * Creates an instance based on a request.
   * <p>
   * The request will be altered during the iteration.
   * 
   * @param <R>  the type of the configuration item
   * @param master  the underlying master, not null
   * @param request  the request object, not null
   * @return an iterable suitable for use in a for-each loop, not null
   */
  public static <R> Iterable<ConfigItem<R>> iterable(final ConfigMaster master, final ConfigSearchRequest<R> request) {
    ArgumentChecker.notNull(master, "master");
    ArgumentChecker.notNull(request, "request");
    return new Iterable<ConfigItem<R>>() {
      @Override
      public Iterator<ConfigItem<R>> iterator() {
        return new ConfigMasterIterator<R>(master, request);
      }
    };
  }

  /**
   * Creates an instance based on a request.
   * <p>
   * The request will be altered during the iteration.
   * 
   * @param master  the underlying master, not null
   * @param request  the request object, not null
   */
  public ConfigMasterIterator(ConfigMaster master, ConfigSearchRequest<T> request) {
    ArgumentChecker.notNull(master, "master");
    ArgumentChecker.notNull(request, "request");
    _master = master;
    _request = request;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean hasNext() {
    if (_currentBatch == null || _batchIndex >= _currentBatch.getDocuments().size()) {
      doFetch();
    }
    return (_currentBatch != null && _batchIndex < _currentBatch.getDocuments().size());
  }

  @Override
  public ConfigItem<T> next() {
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
      _currentBatch = _master.search(_request);
      
    } catch (RuntimeException ex) {
      int totalItems = _overallIndex + 5;  // if we have never got results back, allow for 5 failures
      if (_currentBatch != null) {
        totalItems = _currentBatch.getPaging().getTotalItems();  // if we have results, use maximum count
      }
      while (_overallIndex <= totalItems) {
        try {
          _request.setPagingRequest(PagingRequest.ofIndex(_overallIndex, 1));
          _currentBatch = _master.search(_request);
        } catch (RuntimeException ex2) {
          _overallIndex++;
        }
      }
      if (_overallIndex >= totalItems) {
        throw new OpenGammaRuntimeException("Multiple documents failed to load", ex);
      }
    }
    
    // ensure same vc for whole iterator
    _request.setVersionCorrection(_currentBatch.getVersionCorrection());
    
    // check results
    if (_currentBatch.getPaging().getFirstItem() < _overallIndex) {
      _batchIndex = (_overallIndex - _currentBatch.getPaging().getFirstItem());
    } else {
      _batchIndex = 0;
    }
  }

  @SuppressWarnings("unchecked")
  private ConfigItem<T> doNext() {
    _current = (ConfigItem<T>) _currentBatch.getDocuments().get(_batchIndex).getObject();
    _batchIndex++;
    _overallIndex++;
    return _current;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the underlying master.
   * 
   * @return the master, not null
   */
  public ConfigMaster getMaster() {
    return _master;
  }

  /**
   * Gets the request object that is being used.
   * 
   * @return the request, not null
   */
  public ConfigSearchRequest<T> getRequest() {
    return _request;
  }

}
