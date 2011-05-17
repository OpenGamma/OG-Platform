/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.id.UniqueIdentifier;

/**
 *  
 */
public class ViewResultEntryMapper {
  
  public static String sqlGet() {
    return "select " +
      "comp_target_type, " +
      "comp_target_id_scheme, " +
      "comp_target_id_value, " +
      "comp_target_id_version, " +
      "calc_conf_name, " +
      "name, " +
      "function_unique_id, " +
      "value " +
      " from " + DbBatchMaster.getDatabaseSchema() + "vw_rsk where" +
      " rsk_run_id = :rsk_run_id";
  }
  
  public static String sqlCount() {
    return "select count(*) " + 
      " from " + DbBatchMaster.getDatabaseSchema() + "vw_rsk where" +
      " rsk_run_id = :rsk_run_id";
  }
  
  /**
   * Spring ParameterizedRowMapper 
   */
  public static final RowMapper<ViewResultEntry> ROW_MAPPER = new RowMapper<ViewResultEntry>() {
    @Override
    public ViewResultEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
      
      ComputationTargetType computationTargetType = ComputationTargetType.valueOf(rs.getString("comp_target_type"));
      
      UniqueIdentifier targetIdentifier = UniqueIdentifier.of(
          rs.getString("comp_target_id_scheme"), 
          rs.getString("comp_target_id_value"),
          rs.getString("comp_target_id_version"));
      
      ValueRequirement valueRequirement = new ValueRequirement(
          rs.getString("name"), 
          computationTargetType,
          targetIdentifier);
      
      ValueSpecification valueSpecification = new ValueSpecification(valueRequirement, rs.getString("function_unique_id"));
      
      ComputedValue computedValue = new ComputedValue(valueSpecification, rs.getDouble("value"));
      
      return new ViewResultEntry(rs.getString("calc_conf_name"), computedValue);
    }
  };

}
