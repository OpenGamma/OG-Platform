/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.batch.BatchRunWriter;
import com.opengamma.batch.RunCreationMode;
import com.opengamma.batch.SnapshotMode;
import com.opengamma.batch.domain.MarketData;
import com.opengamma.batch.domain.MarketDataValue;
import com.opengamma.batch.domain.RiskRun;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * {@link BatchRunWriter} that decorates another instance and dispatches all operations using a single threaded
 * executor. This allows an implementation that isn't thread safe (e.g. {@link DbBatchMaster}) to be used by
 * multiple threads concurrently.
 */
public class SingleThreadBatchRunWriter implements BatchRunWriter {

  private final BatchRunWriter _delegate;
  private final ExecutorService _executor = Executors.newSingleThreadExecutor();

  /**
   * Creates a thread safe {@link BatchRunWriter} that forwards method calls to {@code delegate}.
   * @param delegate Performs the actual writing of the data
   */
  public SingleThreadBatchRunWriter(BatchRunWriter delegate) {
    ArgumentChecker.notNull(delegate, "delegate");
    _delegate = delegate;
  }

  @Override
  public RiskRun startRiskRun(final ViewCycleMetadata cycleMetadata,
                              final Map<String, String> batchParameters,
                              final RunCreationMode runCreationMode,
                              final SnapshotMode snapshotMode) {
    Callable<RiskRun> callable = new Callable<RiskRun>() {
      public RiskRun call() throws Exception {
        return _delegate.startRiskRun(cycleMetadata, batchParameters, runCreationMode, snapshotMode);
      }
    };
    try {
      return _executor.submit(callable).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new OpenGammaRuntimeException("Failed to to start risk run", e);
    }
  }

  @Override
  public void endRiskRun(final ObjectId batchUniqueId) {
    Runnable runnable = new Runnable() {
      public void run() {
        _delegate.endRiskRun(batchUniqueId);
      }
    };
    _executor.execute(runnable);
  }

  @Override
  public void addJobResults(final ObjectId riskRunId, final ViewComputationResultModel result) {
    Runnable runnable = new Runnable() {
      public void run() {
        _delegate.addJobResults(riskRunId, result);
      }
    };
    _executor.execute(runnable);
  }

  @Override
  public MarketData createMarketData(final UniqueId marketDataUid) {
    Callable<MarketData> callable = new Callable<MarketData>() {
      public MarketData call() throws Exception {
        return _delegate.createMarketData(marketDataUid);
      }
    };
    try {
      return _executor.submit(callable).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new OpenGammaRuntimeException("Failed to create market data", e);
    }
  }

  @Override
  public void addValuesToMarketData(final ObjectId marketDataId, final Set<MarketDataValue> values) {
    Runnable runnable = new Runnable() {
      public void run() {
        _delegate.addValuesToMarketData(marketDataId, values);
      }
    };
    _executor.execute(runnable);
  }

  @Override
  public void deleteMarketData(final ObjectId marketDataId) {
    Runnable runnable = new Runnable() {
      public void run() {
        _delegate.deleteMarketData(marketDataId);
      }
    };
    _executor.execute(runnable);
  }
}
