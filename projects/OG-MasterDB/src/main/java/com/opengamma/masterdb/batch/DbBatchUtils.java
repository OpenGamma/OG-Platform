/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import com.opengamma.batch.domain.ComputeFailure;
import com.opengamma.batch.domain.ComputeFailureKey;
import com.opengamma.batch.domain.FailureReason;
import com.opengamma.batch.domain.RiskFailure;
import com.opengamma.batch.domain.RiskValue;
import com.opengamma.batch.domain.StatusEntry;

/**
 * Utilities for converting to and from batch types.
 */
public class DbBatchUtils {

  public static SqlParameterSource toSqlParameterSource(ComputeFailureKey computeFailureKey) {
    MapSqlParameterSource source = new MapSqlParameterSource();
    source.addValue("function_id", computeFailureKey.getFunctionId());
    source.addValue("exception_class", computeFailureKey.getExceptionClass());
    source.addValue("exception_msg", computeFailureKey.getExceptionMsg());
    source.addValue("stack_trace", computeFailureKey.getStackTrace());
    return source;
  }

  public static SqlParameterSource toSqlParameterSource(ComputeFailure computeFailure) {
    MapSqlParameterSource source = new MapSqlParameterSource();
    source.addValue("id", computeFailure.getId());
    source.addValue("function_id", computeFailure.getFunctionId());
    source.addValue("exception_class", computeFailure.getExceptionClass());
    source.addValue("exception_msg", computeFailure.getExceptionMsg());
    source.addValue("stack_trace", computeFailure.getStackTrace());
    return source;
  }

  public SqlParameterSource toSqlParameterSource(FailureReason failureReason) {
    MapSqlParameterSource source = new MapSqlParameterSource();
    source.addValue("id", failureReason.getId());
    source.addValue("rsk_failure_id", failureReason.getRiskFailure().getId());
    source.addValue("compute_failure_id", failureReason.getComputeFailureId());
    return source;
  }

  public SqlParameterSource toSqlParameterSource(RiskFailure riskFailure) {
    MapSqlParameterSource source = new MapSqlParameterSource();
    source.addValue("id", riskFailure.getId());
    source.addValue("calculation_configuration_id", riskFailure.getCalculationConfigurationId());
    source.addValue("name", riskFailure.getName());
    source.addValue("value_requirement_id", riskFailure.getValueRequirementId());
    source.addValue("value_specification_id", riskFailure.getValueSpecificationId());
    source.addValue("function_unique_id", riskFailure.getFunctionUniqueId());
    source.addValue("computation_target_id", riskFailure.getComputationTargetId());
    source.addValue("run_id", riskFailure.getRunId());
    source.addValue("eval_instant", riskFailure.getEvalInstant());
    source.addValue("compute_node_id", riskFailure.getComputeNodeId());
    return source;
  }

  public SqlParameterSource toSqlParameterSource(RiskValue riskValue) {
    MapSqlParameterSource source = new MapSqlParameterSource();
    source.addValue("id", riskValue.getId());
    source.addValue("calculation_configuration_id", riskValue.getCalculationConfigurationId());
    source.addValue("name", riskValue.getName());
    source.addValue("value_specification_id", riskValue.getValueSpecificationId());
    source.addValue("function_unique_id", riskValue.getFunctionUniqueId());
    source.addValue("computation_target_id", riskValue.getComputationTargetId());
    source.addValue("run_id", riskValue.getRunId());
    source.addValue("value", riskValue.getValue());
    source.addValue("eval_instant", riskValue.getEvalInstant());
    source.addValue("compute_node_id", riskValue.getComputeNodeId());
    return source;
  }

  //-------------------------------------------------------------------------
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
