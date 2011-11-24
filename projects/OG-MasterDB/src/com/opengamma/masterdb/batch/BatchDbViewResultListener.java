/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import com.opengamma.engine.view.CycleInfo;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.financial.batch.BatchId;
import com.opengamma.livedata.UserPrincipal;

import javax.time.Instant;

public class BatchDbViewResultListener implements ViewResultListener {

  private Batch _batch;

  private BatchRunMaster _batchRunMaster;

  public BatchDbViewResultListener(BatchRunMaster batchRunMaster) {
    _batchRunMaster = batchRunMaster;
  }

  @Override
  public UserPrincipal getUser() {
    return null;
  }

  @Override
  public void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition, boolean hasMarketDataPermissions) {
    //nothing to do at this stage
  }

  @Override
  public void viewDefinitionCompilationFailed(Instant valuationTime, Exception exception) {
    //The batch will be not started
    //TODO shall we do dummy batch start-stop to indicated unstarted batch?
  }

  @Override
  public void cycleInitiated(CycleInfo cycleInfo) {

    _batch = new Batch(
        new BatchId(
            cycleInfo.getMarketDataSnapshotUniqueId(),
            cycleInfo.getViewDefinition().getUniqueId(),
            cycleInfo.getVersionCorrection(),
            cycleInfo.getValuationTime()),
        cycleInfo);

    _batchRunMaster.startBatch(_batch);
  }

  @Override
  public void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
    _batchRunMaster.endBatch(_batch);
  }

  @Override
  public void jobResultReceived(ViewResultModel fullResult, ViewDeltaResultModel deltaResult) {
    _batchRunMaster.addJobResults(_batch, fullResult);
  }

  @Override
  public void cycleExecutionFailed(ViewCycleExecutionOptions executionOptions, Exception exception) {
    //TODO where to write the exception? Batch run ?
    _batchRunMaster.end(_batch);
  }

  @Override
  public void processCompleted() {
  }

  @Override
  public void processTerminated(boolean executionInterrupted) {
    //TODO Shall we add info to the batchrun that it was interrupted?
    if (executionInterrupted) {
      _batchRunMaster.end(_batch);
    }
  }
}
