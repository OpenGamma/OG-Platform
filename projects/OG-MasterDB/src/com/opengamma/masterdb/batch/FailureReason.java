/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * Hibernate bean.
 */
public class FailureReason {
  
  private long _id;
  private RiskFailure _riskFailure;
  private long _computeFailureId;
  
  public long getId() {
    return _id;
  }
  
  public void setId(long id) {
    _id = id;
  }
  
  public RiskFailure getRiskFailure() {
    return _riskFailure;
  }
  
  public void setRiskFailure(RiskFailure riskFailure) {
    _riskFailure = riskFailure;
  }
  
  public long getComputeFailureId() {
    return _computeFailureId;
  }

  public void setComputeFailureId(long computeFailureId) {
    _computeFailureId = computeFailureId;
  }
  
  public SqlParameterSource toSqlParameterSource() {
    MapSqlParameterSource source = new MapSqlParameterSource();
    source.addValue("id", getId());   
    source.addValue("rsk_failure_id", getRiskFailure().getId());
    source.addValue("compute_failure_id", getComputeFailureId());
    return source;
  }
  
  public static String sqlInsertRiskFailureReason() {
    return "INSERT INTO " + DbBatchMaster.getDatabaseSchema() + "rsk_failure_reason " +
              "(id, rsk_failure_id, compute_failure_id) " +
            "VALUES " +
              "(:id, :rsk_failure_id, :compute_failure_id)";
  }
  
  public static String sqlDeleteRiskFailureReasons() {
    return "DELETE FROM " + DbBatchMaster.getDatabaseSchema() + "rsk_failure_reason WHERE rsk_failure_id in (SELECT id FROM rsk_failure WHERE run_id = :run_id)";
  }
  
  public static String sqlCount() {
    return "SELECT COUNT(*) FROM " + DbBatchMaster.getDatabaseSchema() + "rsk_failure_reason";
  }

}
