/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractChangeProvidingMaster;
import com.opengamma.master.AbstractDocument;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PoolExecutor;
import com.opengamma.util.PoolExecutor.CompletionListener;
import com.opengamma.util.async.BlockingOperation;

/**
 * A partial master implementation that divides search operations into a number of smaller operations to pass to the underlying. This is intended for use with some database backed position masters
 * where performance decreases, or becomes unstable, with large queries.
 * 
 * @param <D> the document type
 * @param <M> the underlying master type
 */
public abstract class AbstractQuerySplittingMaster<D extends AbstractDocument, M extends AbstractChangeProvidingMaster<D>> implements AbstractChangeProvidingMaster<D> {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractQuerySplittingMaster.class);

  private static final PoolExecutor s_executor = new PoolExecutor(8, "QuerySplittingMaster");

  /**
   * The pool executor, if a sub-class does parallel operations.
   */
  private PoolExecutor _executor;

  /**
   * The underlying master to pass requests onto.
   */
  private final M _underlying;

  /**
   * The maximum size of request to pass to {@link AbstractChangeProvidingMaster#get(Collection)}, zero or negative for no limit.
   */
  private int _maxGetRequest;

  /**
   * Creates a new instance wrapping the underlying with default properties.
   * 
   * @param underlying the underlying master to satisfy the requests, not null
   */
  public AbstractQuerySplittingMaster(final M underlying) {
    _underlying = ArgumentChecker.notNull(underlying, "underlying");
    _executor = s_executor;
  }

  public void setPoolExecutor(final PoolExecutor executor) {
    if (executor == null) {
      _executor = s_executor;
    } else {
      _executor = executor;
    }
  }

  public PoolExecutor getPoolExecutor() {
    return _executor;
  }

  protected <T> PoolExecutor.Service<T> parallelService(final PoolExecutor.CompletionListener<T> listener) {
    return getPoolExecutor().createService(listener);
  }

  /**
   * Returns the underlying master.
   * 
   * @return the master, not null
   */
  protected M getUnderlying() {
    return _underlying;
  }

  /**
   * Tests whether it is sensible to attempt to split a request.
   * <p>
   * When the database connection pool is running in a polling mode (throwing an exception when there is no immediately available connection so alternative work can be done instead of blocking) then
   * splitting queries may mean that the composite never completes on a busy system.
   * 
   * @return true to attempt to split the operation, false otherwise
   */
  protected boolean canSplit() {
    return BlockingOperation.isOn();
  }

  /**
   * Returns the maximum number of items to pass to the {@link AbstractChangeProvidingMaster#get(Collection)} method in each call.
   * 
   * @return the current limit, zero or negative if none
   */
  public int getMaxGetRequest() {
    return _maxGetRequest;
  }

  /**
   * Sets the maximum number of items to pass to the {@link AbstractChangeProvidingMaster#get(Collection)} method in each call.
   * 
   * @param maxGetRequest the new limit, zero or negative if none
   */
  public void setMaxGetRequest(final int maxGetRequest) {
    _maxGetRequest = maxGetRequest;
  }

  /**
   * Splits the get request into a number of smaller chunks.
   * 
   * @param uniqueIds the original request, not null
   * @return the collection of smaller requests, or null to not split
   */
  protected Collection<Collection<UniqueId>> splitGetRequest(final Collection<UniqueId> uniqueIds) {
    int chunkSize = getMaxGetRequest();
    final int count = uniqueIds.size();
    if ((chunkSize <= 0) || (chunkSize >= count)) {
      // Request too small, or splitting is disabled
      return null;
    }
    int chunks = (count + chunkSize - 1) / chunkSize;
    final Collection<Collection<UniqueId>> requests = new ArrayList<Collection<UniqueId>>();
    final Iterator<UniqueId> itr = uniqueIds.iterator();
    for (int i = 0; i < count;) {
      chunkSize = (count - i) / (chunks--);
      final Collection<UniqueId> request = new ArrayList<UniqueId>(chunkSize);
      for (int j = 0; (j < chunkSize) && itr.hasNext(); j++) {
        request.add(itr.next());
      }
      requests.add(request);
      i += chunkSize;
    }
    return requests;

  }

  /**
   * Combines the results from each call to the underlying.
   * 
   * @param mergeWith the result to return, not null
   * @param result the result from a single call to the underlying
   */
  protected void mergeSplitGetResult(final Map<UniqueId, D> mergeWith, final Map<UniqueId, D> result) {
    mergeWith.putAll(result);
  }

  /**
   * Makes multiple calls to the underlying, combining them into a single result.
   * 
   * @param requests the requests, typically created by calling {@link #splitGetRequest}.
   * @return the combined result, typically created by calling {@link #mergeSplitGetRequest} with each underlying call result.
   */
  protected Map<UniqueId, D> callSplitGetRequest(final Collection<Collection<UniqueId>> requests) {
    final Map<UniqueId, D> result = new HashMap<UniqueId, D>();
    for (Collection<UniqueId> request : requests) {
      mergeSplitGetResult(result, getUnderlying().get(request));
    }
    return result;
  }

  /**
   * Alternative implementation of {@link #callSplitGetRequest} that sub-classes can use instead.
   * 
   * @param requests the requests, created by calling {@link #splitGetRequest}.
   * @return the combined result, created by calling {@link #mergeSplitGetRequest} with each underlying call result.
   */
  protected Map<UniqueId, D> parallelSplitGetRequest(final Collection<Collection<UniqueId>> requests) {
    final Map<UniqueId, D> mergedResult = new HashMap<UniqueId, D>();
    final PoolExecutor.Service<Map<UniqueId, D>> service = parallelService(new CompletionListener<Map<UniqueId, D>>() {

      @Override
      public void success(final Map<UniqueId, D> result) {
        synchronized (mergedResult) {
          mergeSplitGetResult(mergedResult, result);
        }
      }

      @Override
      public void failure(final Throwable error) {
        s_logger.error("Caught exception", error);
      }

    });
    s_logger.debug("Issuing {} parallel queries", requests.size());
    long t = System.nanoTime();
    for (final Collection<UniqueId> request : requests) {
      service.execute(new Callable<Map<UniqueId, D>>() {
        @Override
        public Map<UniqueId, D> call() throws Exception {
          s_logger.debug("Requesting {} records", request.size());
          long t = System.nanoTime();
          final Map<UniqueId, D> result = getUnderlying().get(request);
          s_logger.info("{} records queried in {}ms", request.size(), (double) (System.nanoTime() - t) / 1e6);
          return result;
        }
      });
    }
    try {
      service.join();
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("Interrupted", e);
    }
    s_logger.info("Finished queries for {} records in {}ms", mergedResult.size(), (double) (System.nanoTime() - t) / 1e6);
    return mergedResult;
  }

  // AbstractChangeProvidingMaster

  @Override
  public D get(final UniqueId uniqueId) {
    return getUnderlying().get(uniqueId);
  }

  @Override
  public D get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    return getUnderlying().get(objectId, versionCorrection);
  }

  /**
   * When splitting is enabled, and the request is for more than the split size, two or more requests are made to the underlying master. {@inheritDoc}
   */
  @Override
  public Map<UniqueId, D> get(final Collection<UniqueId> uniqueIds) {
    if (canSplit()) {
      final Collection<Collection<UniqueId>> requests = splitGetRequest(uniqueIds);
      if (requests == null) {
        // Small query pass-through
        return getUnderlying().get(uniqueIds);
      } else {
        // Multiple queries
        return callSplitGetRequest(requests);
      }
    } else {
      // Splitting disabled
      return getUnderlying().get(uniqueIds);
    }
  }

  @Override
  public D add(final D document) {
    return getUnderlying().add(document);
  }

  @Override
  public D update(final D document) {
    return getUnderlying().update(document);
  }

  @Override
  public void remove(final ObjectIdentifiable oid) {
    getUnderlying().remove(oid);
  }

  @Override
  public D correct(final D document) {
    return getUnderlying().correct(document);
  }

  @Override
  public List<UniqueId> replaceVersion(final UniqueId uniqueId, final List<D> replacementDocuments) {
    return getUnderlying().replaceVersion(uniqueId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(final ObjectIdentifiable objectId, final List<D> replacementDocuments) {
    return getUnderlying().replaceAllVersions(objectId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(final ObjectIdentifiable objectId, final List<D> replacementDocuments) {
    return getUnderlying().replaceVersions(objectId, replacementDocuments);
  }

  @Override
  public UniqueId replaceVersion(final D replacementDocument) {
    return getUnderlying().replaceVersion(replacementDocument);
  }

  @Override
  public void removeVersion(final UniqueId uniqueId) {
    getUnderlying().removeVersion(uniqueId);
  }

  @Override
  public UniqueId addVersion(final ObjectIdentifiable objectId, final D documentToAdd) {
    return getUnderlying().addVersion(objectId, documentToAdd);
  }

  @Override
  public ChangeManager changeManager() {
    return getUnderlying().changeManager();
  }

}
