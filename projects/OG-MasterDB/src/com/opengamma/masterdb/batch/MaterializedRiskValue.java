/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 *  
 */
public class MaterializedRiskValue {
  
  private final String _calculationConfiguration;
  private final ComputedValue _computedValue;
  
  public MaterializedRiskValue(String calculationConfiguration,
      ComputedValue computedValue) {
    ArgumentChecker.notNull(calculationConfiguration, "calculationConfiguration");
    ArgumentChecker.notNull(computedValue, "computedValue");
    
    _calculationConfiguration = calculationConfiguration;
    _computedValue = computedValue;
  }
  
  public String getCalculationConfiguration() {
    return _calculationConfiguration;
  }

  public ComputedValue getComputedValue() {
    return _computedValue;
  }

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
      " from vw_rsk where" +
      " rsk_run_id = :rsk_run_id";
  }
  
  /**
   * Spring ParameterizedRowMapper 
   */
  public static final RowMapper<MaterializedRiskValue> ROW_MAPPER = new RowMapper<MaterializedRiskValue>() {
    @Override
    public MaterializedRiskValue mapRow(ResultSet rs, int rowNum) throws SQLException {
      
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
      
      return new MaterializedRiskValue(rs.getString("calc_conf_name"), computedValue);
    }
  };

}
