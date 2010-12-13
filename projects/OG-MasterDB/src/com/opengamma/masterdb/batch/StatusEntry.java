/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * 
 */
public class StatusEntry {
  
  /**
   * Status constants
   */
  public enum Status {
    /**
     * The computation has succeeded completely
     * - all risk for this computation target
     * is in the database.
     */
    SUCCESS, 
    
    /**
     * The computation has failed, wholly or partially
     * - in either case, no risk for this computation
     * target is in the database.
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
  private int _calculationConfigurationId;
  private int _computationTargetId;
  private Status _status;
  
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
  
  public static String sqlGet() {
    return "SELECT id, calculation_configuration_id, computation_target_id, status FROM " + BatchDbManagerImpl.getDatabaseSchema() + "rsk_run_status WHERE " +
      "calculation_configuration_id = :calculation_configuration_id AND " +
      "computation_target_id = :computation_target_id";         
  }
  
  public static String sqlUpdate() {
    return "UPDATE " + BatchDbManagerImpl.getDatabaseSchema() + "rsk_run_status SET status = :status WHERE " +
      "id = :id";
  }
  
  public static String sqlInsert() {
    return "INSERT INTO " + BatchDbManagerImpl.getDatabaseSchema() + "rsk_run_status " +
      "(id, calculation_configuration_id, computation_target_id, status) VALUES (" +
      ":id, :calculation_configuration_id, :computation_target_id, :status)";      
  }
  
  /**
   * Spring ParameterizedRowMapper 
   */
  public static final RowMapper<StatusEntry> ROW_MAPPER = new RowMapper<StatusEntry>() {
    @Override
    public StatusEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
      StatusEntry statusEntry = new StatusEntry();
      statusEntry.setId(rs.getLong("id"));
      statusEntry.setCalculationConfigurationId(rs.getInt("calculation_configuration_id"));
      statusEntry.setComputationTargetId(rs.getInt("computation_target_id"));
      statusEntry.setStatus(rs.getInt("status"));
      return statusEntry;
    }
  };
  
}
