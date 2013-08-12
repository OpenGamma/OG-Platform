/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.batch.domain;

/**
 * Data model for a status entry.
 */
public class StatusEntry {
  
  /**
   * Status constants
   */
  public enum Status {
    /**
     * The computation for this target has succeeded so far, or completely if the run has finished.
     */
    SUCCESS, 
    
    /**
     * The computation has failed, wholly or partially. Successfully-calculated results for this target are still
     * present in the database.
     */
    FAILURE, 
    
    /**
     * The computation is running. As of now, unused.
     */
    RUNNING, 

    /**
     * We know that this computation needs to be performed,
     * but it is not yet running. As of now, unused. 
     */
    NOT_RUNNING
  }
  
  
  private long _id = -1;
  private long _runId = -1;
  private long _calculationConfigurationId;
  private long _computationTargetId;
  private Status _status;
  
  public long getId() {
    return _id;
  }
  
  public void setId(long id) {
    _id = id;
  }

  public long getRunId() {
    return _runId;
  }

  public void setRunId(long runId) {
    _runId = runId;
  }

  public long getCalculationConfigurationId() {
    return _calculationConfigurationId;
  }
  
  public void setCalculationConfigurationId(long calculationConfigurationId) {
    _calculationConfigurationId = calculationConfigurationId;
  }
  
  public long getComputationTargetId() {
    return _computationTargetId;
  }
  
  public void setComputationTargetId(long computationTargetId) {
    _computationTargetId = computationTargetId;
  }
  
  public Status getStatus() {
    return _status;
  }
  
  public void setStatus(Status status) {
    _status = status;
  }
  
  public void setStatus(int statusInt) {
    for (Status status : Status.values()) {
      if (status.ordinal() == statusInt) {
        _status = status;
        return;
      }
    }
    throw new IllegalArgumentException(statusInt + " is not a valid status");
  }
}
