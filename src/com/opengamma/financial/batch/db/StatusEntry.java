/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch.db;

/**
 * 
 */
public class StatusEntry {
  
  /**
   * The computation has succeeded completely
   * - all risk for this computation target
   * is in the database.
   */
  public static final int SUCCESS = 0;
  
  /**
   * The computation has failed, wholly or partially
   * - in either case, no risk for this computation
   * target is in the database.
   */
  public static final int FAILURE = 1;
  
  /**
   * The computation is running. As of now, unused.
   */
  public static final int RUNNING = 2;

  /**
   * We know that this computation needs to be performed,
   * but it is not yet running. As of now, unused. 
   */
  public static final int NOT_RUNNING = 3;
  
  private long _id;
  private int _calculationConfigurationId;
  private int _computationTargetId;
  private int _status;
  
  public long getId() {
    return _id;
  }
  
  public void setId(long id) {
    _id = id;
  }
  
  public int getCalculationConfigurationId() {
    return _calculationConfigurationId;
  }
  
  public void setCalculationConfigurationId(int calculationConfigurationId) {
    _calculationConfigurationId = calculationConfigurationId;
  }
  
  public int getComputationTargetId() {
    return _computationTargetId;
  }
  
  public void setComputationTargetId(int computationTargetId) {
    _computationTargetId = computationTargetId;
  }
  
  public int getStatus() {
    return _status;
  }
  
  public void setStatus(int status) {
    _status = status;
  }
  
  public static String sqlGet() {
    return "SELECT id, status FROM rsk_run_status WHERE " +
      "calculation_configuration_id = :calculation_configuration_id AND " +
      "computation_target_id = :computation_target_id";         
  }
  
  public static String sqlUpdate() {
    return "UPDATE rsk_run_status SET status = :status WHERE " +
      "id = :id";
  }
  
  public static String sqlInsert() {
    return "INSERT INTO rsk_run_status " +
      "(id, calculation_configuration_id, computation_target_id, status) VALUES (" +
      ":id, :calculation_configuration_id, :computation_target_id, :status)";      
  }
  
}
