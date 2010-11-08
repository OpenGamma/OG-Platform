/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import java.util.Collection;

import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewInternal;
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
  
  /**
   * Used as a default "observation time" for ad hoc batches, i.e., batches that are
   * started manually by users and whose results should NOT flow to downstream
   * systems.  
   */
  public static final String AD_HOC_OBSERVATION_TIME = "AD_HOC_RUN";
  
  // --------------------------------------------------------------------------
  // Variables initialized at construction time
  // --------------------------------------------------------------------------
  
  /**
   * The job this run belongs to
   */
  private final BatchJob _job;
  
  // --------------------------------------------------------------------------
  
  /** 
   * Why the batch is being run. Would typically tell whether the run is an automatic/manual
   * run, and if manual, who started it and maybe why.
   * 
   * Not null.
   */
  private String _runReason;
  
  /** 
   * A label for the run. Examples: LDN_CLOSE, AD_HOC_RUN. The exact time of LDN_CLOSE could vary
   * daily due to this time being set by the head trader.
   * So one day it might be 16:32, the next 16:46, etc. 
   * 
   * Not null.
   */
  private String _observationTime;
  
  /**
   * What day's batch this is. 
   * 
   * Not null.
   */
  private LocalDate _observationDate;
  
  /**
   * Valuation time for purposes of calculating all risk figures. Often referred to 'T'
   * in mathematical formulas.
   * 
   * Not null.
   */
  private OffsetDateTime _valuationTime;
  
  /**
   * The batch will run against a defined set of market data.
   * 
   * This variable tells which set exactly. The contents are 
   * similar to {@link #_observationTime}.
   * 
   * Not null.
   */
  private String _snapshotObservationTime;

  /**
   * What day's market data snapshot to use.
   * 
   * Not null.
   */
  private LocalDate _snapshotObservationDate;
  
  // --------------------------------------------------------------------------
  // Variables initialized during the batch run
  // --------------------------------------------------------------------------
  
  /**
   * Set by _batchDbManager
   */
  private Object _dbHandle;
  
  /**
   * Whether the run failed due to unexpected exception
   */
  private boolean _failed;

  //--------------------------------------------------------------------------
  
  public BatchJobRun(BatchJob job) {
    ArgumentChecker.notNull(job, "Batch job");
    _job = job;
  }
  
  //--------------------------------------------------------------------------
  
  public String getRunReason() {
    return _runReason;
  }

  public void setRunReason(String runReason) {
    _runReason = runReason;
  }

  public String getObservationTime() {
    return _observationTime;
  }

  public void setObservationTime(String observationTime) {
    _observationTime = observationTime;
  }

  public LocalDate getObservationDate() {
    return _observationDate;
  }

  public void setObservationDate(String observationDate) {
    if (observationDate == null) {
      _observationDate = null;
    } else { 
      _observationDate = parseDate(observationDate);
    }
  }
  
  public void setObservationDate(LocalDate observationDate) {
    _observationDate = observationDate;
  }

  public OffsetDateTime getValuationTime() {
    return _valuationTime;
  }

  public void setValuationTime(String valuationTime) {
    _valuationTime = parseDateTime(valuationTime);
  }
  
  public void setValuationTime(OffsetDateTime valuationTime) {
    _valuationTime = valuationTime;
  }

  public LocalDate getSnapshotObservationDate() {
    return _snapshotObservationDate;
  }

  public void setSnapshotObservationDate(String snapshotObservationDate) {
    if (snapshotObservationDate == null) {
      _snapshotObservationDate = null;
    } else {
      _snapshotObservationDate = parseDate(snapshotObservationDate);
    }
  }
  
  public void setSnapshotObservationDate(LocalDate snapshotObservationDate) {
    _snapshotObservationDate = snapshotObservationDate;
  }
  
  public String getSnapshotObservationTime() {
    return _snapshotObservationTime;
  }
  
  public void setSnapshotObservationTime(String snapshotObservationTime) {
    _snapshotObservationTime = snapshotObservationTime;
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
  
  public OffsetDateTime parseDateTime(String dateTime) {
    return BatchJob.parseDateTime(dateTime);
  }
  
  public LocalDate parseDate(String date) {
    return BatchJob.parseDate(date);
  }
  
  public String getViewOid() {
    return getJob().getViewOid();
  }
  
  public String getViewVersion() {
    return getJob().getViewVersion();    
  } 
  
  public boolean isForceNewRun() {
    return getJob().isForceNewRun();    
  }
  
  public String getOpenGammaVersion() {
    return getJob().getOpenGammaVersion();
  }
  
  public String getOpenGammaVersionHash() {
    return getJob().getOpenGammaVersionHash();
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
  
  // --------------------------------------------------------------------------
  
  public void init() {
    ZonedDateTime now = getJob().getCreationTime();
    
    if (_runReason == null) {
      _runReason = "Manual run started on " + 
        now + " by " + 
        getUser().getUserName();                   
    }
    
    if (_observationDate == null) {
      _observationDate = now.toLocalDate();
    }
    
    if (_observationTime == null) {
      _observationTime = AD_HOC_OBSERVATION_TIME;      
    }

    if (_valuationTime == null) {
      _valuationTime = OffsetDateTime.of(_observationDate, now, now.getOffset()); 
    }
    
    if (_snapshotObservationDate == null) {
      _snapshotObservationDate = _observationDate;
    }
    
    if (_snapshotObservationTime == null) {
      _snapshotObservationTime = _observationTime;
    }
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
