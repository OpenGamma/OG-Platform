/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * 
 */
public class RiskValue {
  
  private long _id;
  private int _calculationConfigurationId;
  private int _valueNameId;
  private int _functionUniqueId;
  private int _computationTargetId;
  private int _runId;
  private double _value;
  private Date _evalInstant;
  private int _computeNodeId; 

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

  public int getValueNameId() {
    return _valueNameId;
  }

  public void setValueNameId(int valueNameId) {
    _valueNameId = valueNameId;
  }
  
  public int getFunctionUniqueId() {
    return _functionUniqueId;
  }

  public void setFunctionUniqueId(int functionUniqueId) {
    _functionUniqueId = functionUniqueId;
  }

  public int getComputationTargetId() {
    return _computationTargetId;
  }

  public void setComputationTargetId(int computationTargetId) {
    _computationTargetId = computationTargetId;
  }

  public int getRunId() {
    return _runId;
  }

  public void setRunId(int runId) {
    _runId = runId;
  }

  public double getValue() {
    return _value;
  }

  public void setValue(double value) {
    _value = value;
  }

  public Date getEvalInstant() {
    return _evalInstant;
  }

  public void setEvalInstant(Date evalInstant) {
    _evalInstant = evalInstant;
  }

  public int getComputeNodeId() {
    return _computeNodeId;
  }

  public void setComputeNodeId(int computeNodeId) {
    _computeNodeId = computeNodeId;
  }
  
  public SqlParameterSource toSqlParameterSource() {
    MapSqlParameterSource source = new MapSqlParameterSource();
    source.addValue("id", getId());   
    source.addValue("calculation_configuration_id", getCalculationConfigurationId());
    source.addValue("value_name_id", getValueNameId());
    source.addValue("function_unique_id", getFunctionUniqueId());
    source.addValue("computation_target_id", getComputationTargetId());
    source.addValue("run_id", getRunId());
    source.addValue("value", getValue());
    source.addValue("eval_instant", getEvalInstant());
    source.addValue("compute_node_id", getComputeNodeId());
    return source;
  }
  
  public static String sqlInsertRisk() {
    return "INSERT INTO " + BatchDbManagerImpl.getDatabaseSchema() + "rsk_value " +
              "(id, calculation_configuration_id, value_name_id, function_unique_id, computation_target_id, run_id, value, " +
              "eval_instant, compute_node_id) " +
            "VALUES " +
              "(:id, :calculation_configuration_id, :value_name_id, :function_unique_id, :computation_target_id, :run_id, :value," +
              ":eval_instant, :compute_node_id)";
  }
  
  public static String sqlCount() {
    return "SELECT COUNT(*) FROM " + BatchDbManagerImpl.getDatabaseSchema() + "rsk_value";
  }
  
  public static String sqlGet() {
    return "SELECT * from " + BatchDbManagerImpl.getDatabaseSchema() + "rsk_value WHERE " +
      "calculation_configuration_id = :calculation_configuration_id AND " +
      "value_name_id = :value_name_id AND " +
      "computation_target_id = :computation_target_id";
  }
  
  /**
   * Spring ParameterizedRowMapper 
   */
  public static final RowMapper<RiskValue> ROW_MAPPER = new RowMapper<RiskValue>() {
    @Override
    public RiskValue mapRow(ResultSet rs, int rowNum) throws SQLException {
      RiskValue riskValue = new RiskValue();
      riskValue.setId(rs.getLong("id"));
      riskValue.setCalculationConfigurationId(rs.getInt("calculation_configuration_id"));
      riskValue.setValueNameId(rs.getInt("value_name_id"));
      riskValue.setFunctionUniqueId(rs.getInt("function_unique_id"));
      riskValue.setComputationTargetId(rs.getInt("computation_target_id"));
      riskValue.setRunId(rs.getInt("run_id"));
      riskValue.setValue(rs.getDouble("value"));
      riskValue.setEvalInstant(rs.getDate("eval_instant"));
      riskValue.setComputeNodeId(rs.getInt("compute_node_id"));
      return riskValue;
    }
  };
  
}
