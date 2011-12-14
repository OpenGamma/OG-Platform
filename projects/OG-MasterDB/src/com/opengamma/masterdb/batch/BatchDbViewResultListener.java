/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import com.opengamma.engine.view.CycleInfo;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.livedata.UserPrincipal;

import javax.time.Instant;

public class BatchDbViewResultListener implements ViewResultListener {

  private Batch _batch;

  private BatchRunWriter _batchRunWriter;

  public BatchDbViewResultListener(BatchRunWriter batchRunWriter) {
    _batchRunWriter = batchRunWriter;
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

    _batch = new Batch(cycleInfo);

    _batchRunWriter.startBatch(_batch, RunCreationMode.AUTO, SnapshotMode.WRITE_TROUGH);
  }

  @Override
  public void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
    _batchRunWriter.endBatch(_batch.getUniqueId());
  }

  @Override
  public void cycleFragmentCompleted(ViewComputationResultModel fullFragment, ViewDeltaResultModel deltaFragment) {
    _batchRunWriter.addJobResults(_batch.getUniqueId(), fullFragment);
  }

  @Override
  public void cycleExecutionFailed(ViewCycleExecutionOptions executionOptions, Exception exception) {
    //TODO where to write the exception? Batch run ?
    _batchRunWriter.endBatch(_batch.getUniqueId());
  }

  @Override
  public void processCompleted() {
  }

  @Override
  public void processTerminated(boolean executionInterrupted) {
    //TODO Shall we add info to the batchrun that it was interrupted?
    if (executionInterrupted) {
      _batchRunWriter.endBatch(_batch.getUniqueId());
    }
  }
}
