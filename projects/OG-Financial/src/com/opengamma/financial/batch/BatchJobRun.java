/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.time.Instant;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewInternal;
import com.opengamma.financial.batch.BatchJob.RunCreationMode;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * A batch for a single day. A single BatchJob can have several runs, one for each day
 * the batch is to be run. This is useful for example if a client wants to run
 * a historical restatement for 2 years because they have fixed a bug in their
 * pricing models.
 */
public class BatchJobRun {
  
  // --------------------------------------------------------------------------
  // Variables initialized at construction time
  // --------------------------------------------------------------------------
  
  /**
   * The job this run belongs to.
   */
  private final BatchJob _job;
  
  /**
   * What day's batch this is. 
   */
  private final LocalDate _observationDate;
  
  /**
   * What day's market data snapshot to use. 99.9% of the time will be the same as
   * _observationDate.
   */
  private final LocalDate _snapshotObservationDate;
  
  /**
   * Valuation time for purposes of calculating all risk figures. Often referred to as 'T'
   * in mathematical formulas.
   * 
   * Not null.
   */
  private final ZonedDateTime _valuationTime;
  
  /**
   * Historical time used for loading entities out of Config DB.
   */
  private final ZonedDateTime _configDbTime;
  
  /**
   * Historical time used for loading entities out of PositionMaster,
   * SecurityMaster, etc.
   */
  private final ZonedDateTime _staticDataTime;
  
  // --------------------------------------------------------------------------
  // Variables initialized during the batch run
  // --------------------------------------------------------------------------
  
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
  
  /**
   * Whether the run failed due to unexpected exception
   */
  private boolean _failed;

  //--------------------------------------------------------------------------
  
  /**
   * This constructor is useful in tests.
   * 
   * @param job Batch job
   */
  public BatchJobRun(BatchJob job) {
    this(job,
        job.getCreationTime().toLocalDate(),
        job.getCreationTime().toLocalDate(),
        job.getCreationTime().toLocalDate(),
        job.getCreationTime().toLocalDate());
  }
  
  public BatchJobRun(BatchJob job, 
      LocalDate observationDate, 
      LocalDate snapshotObservationDate,
      LocalDate configDbDate,
      LocalDate staticDataDate) {
    ArgumentChecker.notNull(job, "Batch job");
    ArgumentChecker.notNull(observationDate, "Observation date");
    ArgumentChecker.notNull(snapshotObservationDate, "Snapshot observation date");
    ArgumentChecker.notNull(configDbDate, "Config DB date");
    ArgumentChecker.notNull(staticDataDate, "Static data date");
    
    _job = job;
    _observationDate = observationDate;
    _snapshotObservationDate = snapshotObservationDate;
    
    _valuationTime = ZonedDateTime.of(
        observationDate, 
        job.getParameters().getValuationTimeObject(), 
        job.getParameters().getTimeZoneObject());
    
    _configDbTime = ZonedDateTime.of(
        configDbDate,
        job.getParameters().getConfigDbTimeObject(),
        job.getParameters().getTimeZoneObject());
    
    _staticDataTime = ZonedDateTime.of(
        staticDataDate,
        job.getParameters().getStaticDataTimeObject(),
        job.getParameters().getTimeZoneObject());
  }
  
  //--------------------------------------------------------------------------
  
  public String getRunReason() {
    return getJob().getParameters().getRunReason();
  }

  public String getObservationTime() {
    return getJob().getParameters().getObservationTime();
  }

  public LocalDate getObservationDate() {
    return _observationDate;
  }

  public ZonedDateTime getValuationTime() {
    return _valuationTime;
  }
  
  public ZonedDateTime getConfigDbTime() {
    return _configDbTime;
  }

  public ZonedDateTime getStaticDataTime() {
    return _staticDataTime;
  }

  public LocalDate getSnapshotObservationDate() {
    return _snapshotObservationDate;
  }

  public String getSnapshotObservationTime() {
    return getJob().getParameters().getSnapshotObservationTime();
  }

  public SnapshotId getSnapshotId() {
    return new SnapshotId(getSnapshotObservationDate(), getSnapshotObservationTime());
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
  
  public boolean isFailed() {
    return _failed;
  }

  public void setFailed(boolean failed) {
    _failed = failed;
  }

  public BatchJob getJob() {
    return _job;
  }

  public UserPrincipal getUser() {
    return getJob().getUser();
  }
  
  public RunCreationMode getRunCreationMode() {
    return getJob().getRunCreationMode();
  }
  
  public String getOpenGammaVersion() {
    return getJob().getOpenGammaVersion();
  }
  
  public Collection<ViewCalculationConfiguration> getCalculationConfigurations() {
    return getJob().getCalculationConfigurations();
  }
  
  public ViewInternal getView() {
    return getJob().getView();
  }
  
  public ZonedDateTime getCreationTime() {
    return getJob().getCreationTime();
  }
  
  public Map<String, String> getParameters() {
    Map<String, String> jobLevelParameters = getJob().getParameters().getParameters();
    
    Map<String, String> allParameters = new HashMap<String, String>(jobLevelParameters);
    allParameters.put("valuationInstant", getValuationTime().toInstant().toString());
    allParameters.put("configDbInstant", getConfigDbTime().toInstant().toString());
    allParameters.put("staticDataInstant", getStaticDataTime().toInstant().toString());
    
    return allParameters;
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
