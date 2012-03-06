/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import javax.time.Instant;

import com.google.common.collect.Maps;
import com.opengamma.batch.BatchRunWriter;
import com.opengamma.batch.RunCreationMode;
import com.opengamma.batch.SnapshotMode;
import com.opengamma.batch.domain.RiskRun;
import com.opengamma.engine.view.CycleInfo;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.livedata.UserPrincipal;

public class BatchDbViewResultListener implements ViewResultListener {

  private RiskRun _riskRun;

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
    //TODO shall we do dummy batch start-stop to indicate unstarted batch?
  }

  @Override
  public void cycleInitiated(CycleInfo cycleInfo) {
    _riskRun = _batchRunWriter.startRiskRun(cycleInfo, Maps.<String, String>newHashMap(), RunCreationMode.AUTO, SnapshotMode.WRITE_THROUGH);
  }

  @Override
  public void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
    _batchRunWriter.endRiskRun(_riskRun.getObjectId());
  }

  @Override
  public void cycleFragmentCompleted(ViewComputationResultModel fullFragment, ViewDeltaResultModel deltaFragment) {
    _batchRunWriter.addJobResults(_riskRun.getObjectId(), fullFragment);
  }

  @Override
  public void cycleExecutionFailed(ViewCycleExecutionOptions executionOptions, Exception exception) {
    _batchRunWriter.endRiskRun(_riskRun.getObjectId());
  }

  @Override
  public void processCompleted() {
  }

  @Override
  public void processTerminated(boolean executionInterrupted) {
    //TODO Shall we add info to the batchrun that it was interrupted?
    if (executionInterrupted) {
      _batchRunWriter.endRiskRun(_riskRun.getObjectId());
    }
  }
}
