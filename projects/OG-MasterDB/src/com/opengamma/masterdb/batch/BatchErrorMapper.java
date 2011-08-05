/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.financial.batch.BatchError;
import com.opengamma.id.UniqueId;

/**
 * Hibernate helper.
 */
public class BatchErrorMapper {
  
  public static String sqlGet() {
    return "select " +
      "comp_target_type, " +
      "comp_target_id_scheme, " +
      "comp_target_id_value, " +
      "comp_target_id_version, " +
      "calc_conf_name, " +
      "name, " +
      "function_unique_id, " +
      "exception_class," +
      "exception_msg," +
      "stack_trace " +
      " from " + DbBatchMaster.getDatabaseSchema() + "vw_rsk_failure where" +
      " rsk_run_id = :rsk_run_id";
  }
  
  public static String sqlCount() {
    return "select count(*) " + 
      " from " + DbBatchMaster.getDatabaseSchema() + "vw_rsk_failure where" +
      " rsk_run_id = :rsk_run_id";
  }
  
  /**
   * Spring ParameterizedRowMapper 
   */
  public static final RowMapper<BatchError> ROW_MAPPER = new RowMapper<BatchError>() {
    @Override
    public BatchError mapRow(ResultSet rs, int rowNum) throws SQLException {
      
      ComputationTargetType computationTargetType = ComputationTargetType.valueOf(rs.getString("comp_target_type"));
      
      UniqueId targetId = UniqueId.of(
          rs.getString("comp_target_id_scheme"), 
          rs.getString("comp_target_id_value"),
          rs.getString("comp_target_id_version"));
      
      return new BatchError(
          rs.getString("calc_conf_name"),
          new ComputationTargetSpecification(computationTargetType, targetId),
          rs.getString("name"),
          rs.getString("function_unique_id"),
          rs.getString("exception_class"),
          rs.getString("exception_msg"),
          rs.getString("stack_trace"));
    }
  };

}
