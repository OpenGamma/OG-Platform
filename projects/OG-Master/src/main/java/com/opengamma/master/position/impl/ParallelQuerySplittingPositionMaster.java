/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.util.PoolExecutor;
import com.opengamma.util.PoolExecutor.CompletionListener;

/**
 * A {@link QuerySplittingPositionMaster} implementation that makes the underlying requests in parallel.
 */
public class ParallelQuerySplittingPositionMaster extends QuerySplittingPositionMaster {

  private static final Logger s_logger = LoggerFactory.getLogger(ParallelQuerySplittingPositionMaster.class);

  public ParallelQuerySplittingPositionMaster(final PositionMaster underlying) {
    super(underlying);
  }

  @Override
  protected Map<UniqueId, PositionDocument> callSplitGetRequest(final Collection<Collection<UniqueId>> requests) {
    return super.parallelSplitGetRequest(requests);
  }

  @Override
  protected PositionSearchResult callSplitSearchRequest(final Collection<PositionSearchRequest> requests) {
    final PositionSearchResult mergedResult = new PositionSearchResult();
    final PoolExecutor.Service<PositionSearchResult> service = parallelService(new CompletionListener<PositionSearchResult>() {

      @Override
      public void success(final PositionSearchResult result) {
        synchronized (mergedResult) {
          mergeSplitSearchResult(mergedResult, result);
        }
      }

      @Override
      public void failure(final Throwable error) {
        s_logger.error("Caught exception", error);
      }

    });
    s_logger.debug("Issuing {} parallel queries", requests.size());
    long t = System.nanoTime();
    for (final PositionSearchRequest request : requests) {
      service.execute(new Callable<PositionSearchResult>() {
        @Override
        public PositionSearchResult call() throws Exception {
          s_logger.debug("Requesting {} positions", request.getPositionObjectIds().size());
          long t = System.nanoTime();
          final PositionSearchResult result = getUnderlying().search(request);
          s_logger.info("{} positions queried in {}ms", request.getPositionObjectIds().size(), (double) (System.nanoTime() - t) / 1e6);
          return result;
        }
      });
    }
    try {
      service.join();
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("Interrupted", e);
    }
    s_logger.info("Finished queries for {} position in {}ms", mergedResult.getDocuments().size(), (double) (System.nanoTime() - t) / 1e6);
    return mergedResult;
  }

}
