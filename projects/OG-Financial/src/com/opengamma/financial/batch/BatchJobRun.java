/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.time.Instant;
import javax.time.calendar.LocalDate;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * A batch run saved in the database.
 */
public abstract class BatchJobRun {

  /**
   * Identifies the batch in the database.
   */
  private final BatchId _batchId;
  /**
   * When the run was first created in database.
   * If the first run attempt, the value is system clock when the BatchJob object was created.
   * If a second, third... run attempt, it's the system clock on the first run attempt.
   * <p>
   * Set by BatchMaster.
   */
  private Instant _originalCreationTime;
  /**
   * A handle to the database entry for this run.
   * <p>
   * Set by BatchMaster.
   */
  private Object _dbHandle;

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   * 
   * @param batchId  the batch id, not null
   */
  public BatchJobRun(BatchId batchId) {
    ArgumentChecker.notNull(batchId, "Batch database ID");
    _batchId = batchId;
  }

  //-------------------------------------------------------------------------
  public abstract String getRunReason();

  public abstract SnapshotId getSnapshotId();

  public abstract Instant getValuationTime();

  public abstract RunCreationMode getRunCreationMode();

  public abstract String getOpenGammaVersion();

  public abstract Instant getCreationTime();

  public abstract boolean isFailed();

  public abstract Collection<String> getCalculationConfigurations();

  public abstract Collection<ComputationTargetSpecification> getAllComputationTargets();

  public abstract Set<String> getAllOutputValueNames();

  public abstract Map<ValueSpecification, Set<ValueRequirement>> getAllOutputs();

  /**
   * @param spec  one of the targets returned by {@link #getAllComputationTargets()}
   * @return the resolved target, null if full information about this target is not available
   */
  public abstract ComputationTarget resolve(ComputationTargetSpecification spec);

  //-------------------------------------------------------------------------
  /**
   * Gets parameters that do not vary by date.
   * 
   * @return the parameters that do not vary by date, not null
   */
  public abstract Map<String, String> getJobLevelParameters();

  /**
   * Gets parameters that vary by date.
   * 
   * @return the parameters that vary by date, not null
   */
  public Map<String, String> getRunLevelParameters() {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("valuationInstant", getValuationTime().toString());
    return parameters;
  }

  /**
   * Gets the combined set of parameters.
   * 
   * @return the parameters that apply to the run, not null
   */
  public Map<String, String> getParametersMap() {
    Map<String, String> jobLevelParameters = getJobLevelParameters();
    Map<String, String> runLevelParameters = getRunLevelParameters();
    Map<String, String> allParameters = new HashMap<String, String>(jobLevelParameters);
    allParameters.putAll(runLevelParameters);
    return allParameters;
  }

  //-------------------------------------------------------------------------
  public LocalDate getObservationDate() {
    return _batchId.getObservationDate();
  }

  public String getObservationTime() {
    return _batchId.getObservationTime();
  }

  public LocalDate getSnapshotObservationDate() {
    return getSnapshotId().getObservationDate();
  }

  public String getSnapshotObservationTime() {
    return getSnapshotId().getObservationTime();
  }

  public Object getDbHandle() {
    return _dbHandle;
  }

  public void setDbHandle(Object dbHandle) {
    _dbHandle = dbHandle;
  }

  public Instant getOriginalCreationTime() {
    return _originalCreationTime;
  }

  public void setOriginalCreationTime(Instant originalCreationTime) {
    _originalCreationTime = originalCreationTime;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return _batchId.toString();
  }

}
