/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.time.Instant;
import javax.time.calendar.LocalDate;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewInternal;
import com.opengamma.util.ArgumentChecker;

/**
 * A batch run saved in the database.
 */
public abstract class BatchJobRun {
  
  // --------------------------------------------------------------------------
  // Variables initialized at construction time
  // --------------------------------------------------------------------------
  
  /**
   * Identifies the batch in the database 
   */
  private final BatchId _batchId;
  
  /**
   * View associated with this batch
   */
  private ViewInternal _view;
  
  /**
   * When the run was first created in database.
   * If the first run attempt, the value is system clock
   * when the BatchJob object was created.
   * If a second, third, ..., run attempt, it's the 
   * system clock on the first run attempt.
   * <p>
   * Set by BatchDbManager. 
   */
  private Instant _originalCreationTime;

  /**
   * A handle to the database entry for this run.
   * <p>
   * Set by BatchDbManager.
   */
  private Object _dbHandle;
  
  //--------------------------------------------------------------------------
  
  public BatchJobRun(BatchId batchId) {
    ArgumentChecker.notNull(batchId, "Batch database ID");
    _batchId = batchId;
  }
  
  //--------------------------------------------------------------------------
  
  public abstract String getRunReason();
  
  public abstract SnapshotId getSnapshotId();
  
  public abstract Instant getValuationTime();
  
  public abstract RunCreationMode getRunCreationMode();
  
  public abstract String getOpenGammaVersion();
  
  public abstract Instant getCreationTime();
  
  public abstract boolean isFailed();
  
  // --------------------------------------------------------------------------

  /**
   * Gets parameters that do not vary by date.
   * 
   * @return parameters that do not vary by date.
   */
  public abstract Map<String, String> getJobLevelParameters();
  
  /**
   * Gets parameters that vary by date.
   * 
   * @return parameters that vary by date.
   */
  public Map<String, String> getRunLevelParameters() {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("valuationInstant", getValuationTime().toString());
    return parameters;
  }
  
  public Map<String, String> getParametersMap() {
    Map<String, String> jobLevelParameters = getJobLevelParameters();
    Map<String, String> runLevelParameters = getRunLevelParameters();
    
    Map<String, String> allParameters = new HashMap<String, String>(jobLevelParameters);
    allParameters.putAll(runLevelParameters);
    return allParameters;
  }
  
  // --------------------------------------------------------------------------
  
  public LocalDate getObservationDate() {
    return _batchId.getObservationDate();
  }
  
  public String getObservationTime() {
    return  _batchId.getObservationTime();
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
  
  public Collection<ViewCalculationConfiguration> getCalculationConfigurations() {
    return getView().getDefinition().getAllCalculationConfigurations();
  }
  
  public ViewInternal getView() {
    return _view;
  }
  
  public void setView(ViewInternal view) {
    _view = view;
  }

  // --------------------------------------------------------------------------
  
  @Override
  public String toString() {
    return new ToStringBuilder(this)
      .append("Observation date", getObservationDate())
      .append("Observation time", getObservationTime())
      .append("Run reason", getRunReason()).toString(); 
  }

}
