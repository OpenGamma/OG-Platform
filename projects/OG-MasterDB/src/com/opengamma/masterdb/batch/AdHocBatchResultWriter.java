/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.financial.conversion.ResultConverter;
import com.opengamma.financial.conversion.ResultConverterCache;
import com.opengamma.util.db.DbConnector;

/**
 * This writer is used to write risk that originates from an ad hoc batch job.
 * <p>
 * The writer does not keep track of calculation success/failure. In
 * essence, only success results are written. Nothing is written into 
 * table {@code rsk_run_status}. This makes sense because in an ad hoc
 * batch, the user will only choose to save results into batch DB
 * when they are happy with them.
 */
public class AdHocBatchResultWriter extends AbstractBatchResultWriter {
  
  private static final Logger s_logger = LoggerFactory.getLogger(AdHocBatchResultWriter.class);
  
  private final int _computeNodeId;
  
  public AdHocBatchResultWriter(DbConnector dbConnector,
      RiskRun riskRun,
      ComputeNode computeNode,
      ResultConverterCache resultConverterCache,
      Set<ComputationTarget> computationTargets,
      Set<RiskValueRequirement> valueRequirement,
      Set<RiskValueSpecification> valueSpecifications,
      Set<RiskValueName> valueNames) {
    super(dbConnector, riskRun, resultConverterCache, computationTargets, valueNames, valueRequirement, valueSpecifications);
    
    _computeNodeId = computeNode.getId();
  }
  
  public synchronized void write(ViewComputationResultModel resultModel) {
    if (!isInitialized()) {
      initialize();
    }
    
    joinSession();
    writeImpl(resultModel);
  }
  
  @SuppressWarnings("unchecked")
  public void writeImpl(ViewComputationResultModel resultModel) {
    List<SqlParameterSource> successes = new ArrayList<SqlParameterSource>();
    
    int riskRunId = getRiskRunId();

    Map<ValueSpecification, Set<ValueRequirement>> specificationsWithTheirsRequirements = resultModel.getRequirementToSpecificationMapping();
    
    for (ViewResultEntry result : resultModel.getAllResults()) {
      ValueSpecification output = result.getComputedValue().getSpecification();
      Object outputValue = result.getComputedValue().getValue();
      
      int calcConfId = getCalculationConfigurationId(result.getCalculationConfiguration());
      
      ResultConverter<Object> resultConverter;
      try {
        resultConverter = (ResultConverter<Object>) getResultConverterCache().getConverter(outputValue);
      } catch (IllegalArgumentException e) {
        s_logger.info("Could not convert value of type " + outputValue.getClass());
        continue; // with ad hoc batches we accept this 
      }
      
      Map<String, Double> valuesAsDoubles = resultConverter.convert(output.getValueName(), outputValue);
      
      int computationTargetId = getComputationTargetId(output.getTargetSpecification());

      for (Map.Entry<String, Double> riskValueEntry : valuesAsDoubles.entrySet()) {
        int valueNameId = getValueNameId(riskValueEntry.getKey());
        int functionUniqueId = getFunctionUniqueId(output.getFunctionUniqueId());
        Collection<ValueRequirement> requirements = specificationsWithTheirsRequirements.get(output);
        for (ValueRequirement requirement : requirements) {
          int valueRequirementId = getValueRequirementId(requirement.getConstraints());
          int valueSpecificationId = getValueSpecificationId(output.getProperties());
          RiskValue riskValue = new RiskValue();
          riskValue.setId(generateUniqueId());
          riskValue.setCalculationConfigurationId(calcConfId);
          riskValue.setValueNameId(valueNameId);
          riskValue.setValueRequirementId(valueRequirementId);
          riskValue.setValueSpecificationId(valueSpecificationId);
          riskValue.setFunctionUniqueId(functionUniqueId);
          riskValue.setComputationTargetId(computationTargetId);
          riskValue.setRunId(riskRunId);
          riskValue.setValue(riskValueEntry.getValue());
          riskValue.setEvalInstant(new Date(resultModel.getCalculationTime().toEpochMillisLong()));
          riskValue.setComputeNodeId(_computeNodeId);
          successes.add(riskValue.toSqlParameterSource());
        }
      }
    }
    
    if (successes.isEmpty()) {
      s_logger.info("Nothing to write to DB for {}", resultModel);
      return;
    }
    
    // need to figure out why these 2 lines are needed for the insertRows() code not to block...
    // this is bad - loss of transactionality...
    getSessionFactory().getCurrentSession().getTransaction().commit();
    getSessionFactory().getCurrentSession().beginTransaction();
    
    insertRows("risk", RiskValue.sqlInsertRisk(), successes);
  }

}
