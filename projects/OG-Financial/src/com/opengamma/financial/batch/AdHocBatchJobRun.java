/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.time.Instant;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.util.VersionUtil;

/**
 * A batch run where results already exist in memory, but
 * are now to be saved in the database.
 * <p>
 * A typical scenario is that a user plays with Excel, 
 * sets up a view, prices it, and then likes the results
 * enough that they want to save them in the batch DB.
 */
public class AdHocBatchJobRun extends BatchJobRun {
  
  /**
   * The result that already exists in memory
   */
  private final AdHocBatchResult _result;
  
  // --------------------------------------------------------------------------
  
  public AdHocBatchJobRun(AdHocBatchResult result) {
    super(result.getBatchId());
    _result = result;
  }
  
  // --------------------------------------------------------------------------
  
  public ViewComputationResultModel getResultModel() {
    return _result.getResult();
  }
  
  @Override
  public SnapshotId getSnapshotId() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getRunReason() {
    return "Ad hoc run";
  }

  @Override
  public Instant getValuationTime() {
    return getResultModel().getValuationTime();
  }

  @Override
  public RunCreationMode getRunCreationMode() {
    return RunCreationMode.CREATE_NEW_OVERWRITE;
  }

  @Override
  public String getOpenGammaVersion() {
    return VersionUtil.getVersion("og-financial");
  }

  @Override
  public Instant getCreationTime() {
    return getResultModel().getResultTimestamp();
  }

  @Override
  public boolean isFailed() {
    // result already available -> cannot have failed
    return false;
  }

  @Override
  public Map<String, String> getJobLevelParameters() {
    return Collections.emptyMap();
  }

  @Override
  public Collection<String> getCalculationConfigurations() {
    return getResultModel().getCalculationConfigurationNames();
  }

  @Override
  public Set<String> getAllOutputValueNames() {
    return getResultModel().getAllOutputValueNames();
  }

  @Override
  public Collection<ComputationTargetSpecification> getAllComputationTargets() {
    return getResultModel().getAllTargets();
  }

  @Override
  public ComputationTarget resolve(ComputationTargetSpecification spec) {
    return null;
  }
  
}
