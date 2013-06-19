/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.Maps;
import com.opengamma.batch.BatchRunWriter;
import com.opengamma.batch.RunCreationMode;
import com.opengamma.batch.SnapshotMode;
import com.opengamma.batch.domain.RiskRun;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.livedata.UserPrincipal;

/**
 * View result listener implementation for batch runs.
 */
public class BatchDbViewResultListener implements ViewResultListener {

  private static final Logger s_logger = LoggerFactory.getLogger(BatchDbViewResultListener.class);
  
  private final BatchRunWriter _batchRunWriter;
  private final UserPrincipal _user;
  
  private RiskRun _riskRun;

  public BatchDbViewResultListener(BatchRunWriter batchRunWriter, UserPrincipal user) {
    _batchRunWriter = batchRunWriter;
    _user = user;
  }

  @Override
  public UserPrincipal getUser() {
    return _user;
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
  public void cycleStarted(ViewCycleMetadata cycleMetadata) {
    try {
      _riskRun = _batchRunWriter.startRiskRun(cycleMetadata, Maps.<String, String>newHashMap(), RunCreationMode.AUTO, SnapshotMode.WRITE_THROUGH);
    } catch (Exception e) {
      s_logger.error("Failed to write start of batch job. No results will be recorded.", e);
    }
  }

  @Override
  public void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
    endRiskRun();
  }

  @Override
  public void cycleFragmentCompleted(ViewComputationResultModel fullFragment, ViewDeltaResultModel deltaFragment) {
    if (_riskRun == null) {
      s_logger.warn("Skipping writing batch result fragment due to earlier failure to write start of batch job");
      return;
    }
    try {
      _batchRunWriter.addJobResults(_riskRun.getObjectId(), fullFragment);
    } catch (Exception e) {
      s_logger.error("Error writing batch result fragment", e);
    }
  }

  @Override
  public void cycleExecutionFailed(ViewCycleExecutionOptions executionOptions, Exception exception) {
    s_logger.error("Batch cycle execution failed", exception);
    if (_riskRun == null) {
      s_logger.warn("Skipping writing batch cycle failure due to earlier failure to write start of batch job");
      return;
    }
    try {
      _batchRunWriter.endRiskRun(_riskRun.getObjectId());
    } catch (Exception e) {
      s_logger.error("Error writing batch cycle failure", e);
    }
  }

  @Override
  public void processCompleted() {
  }

  @Override
  public void processTerminated(boolean executionInterrupted) {
    if (executionInterrupted) {
      // TODO Shall we add info to the batch run that it was interrupted?
      endRiskRun();
    }
  }

  @Override
  public void clientShutdown(Exception e) {
  }
  
  //-------------------------------------------------------------------------
  private void endRiskRun() {
    if (_riskRun == null) {
      s_logger.warn("Skipping writing end of batch job due to earlier failure to write start of job");
      return;
    }
    try {
      _batchRunWriter.endRiskRun(_riskRun.getObjectId());
    } catch (Exception e) {
      s_logger.error("Failed to write end of batch job. Job will appear incomplete.", e);
    }
  }
  
}
