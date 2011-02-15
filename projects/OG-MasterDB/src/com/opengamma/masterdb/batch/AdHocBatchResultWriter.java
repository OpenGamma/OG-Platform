/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.financial.conversion.ResultConverter;
import com.opengamma.financial.conversion.ResultConverterCache;
import com.opengamma.util.db.DbSource;

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
  
  public AdHocBatchResultWriter(DbSource dbSource,
      RiskRun riskRun,
      ComputeNode computeNode,
      ResultConverterCache resultConverterCache,
      Set<ComputationTarget> computationTargets,
      Set<RiskValueName> valueNames) {
    super(dbSource, riskRun, resultConverterCache, computationTargets, valueNames);
    
    _computeNodeId = computeNode.getId();
  }
  
  public void write(ViewResultModel resultModel) {
    List<SqlParameterSource> successes = new ArrayList<SqlParameterSource>();
    
    int riskRunId = getRiskRunId();
    
    for (ViewResultEntry result : resultModel.getAllResults()) {
      ComputedValue outputValue = result.getComputedValue();
      ValueSpecification output = outputValue.getSpecification();
      
      int calcConfId = getCalculationConfigurationId(result.getCalculationConfiguration());
      
      @SuppressWarnings("unchecked")
      ResultConverter<Object> resultConverter = (ResultConverter<Object>) getResultConverterCache().getConverter(outputValue);
      Map<String, Double> valuesAsDoubles = resultConverter.convert(output.getValueName(), outputValue);
      
      int computationTargetId = getComputationTargetId(output.getTargetSpecification());
      
      for (Map.Entry<String, Double> riskValueEntry : valuesAsDoubles.entrySet()) {
        int valueNameId = getValueNameId(riskValueEntry.getKey());
        int functionUniqueId = getFunctionUniqueId(output.getFunctionUniqueId());
      
        RiskValue riskValue = new RiskValue();
        riskValue.setId(generateUniqueId());
        riskValue.setCalculationConfigurationId(calcConfId);
        riskValue.setValueNameId(valueNameId);
        riskValue.setFunctionUniqueId(functionUniqueId);
        riskValue.setComputationTargetId(computationTargetId);
        riskValue.setRunId(riskRunId);
        riskValue.setValue(riskValueEntry.getValue());
        riskValue.setEvalInstant(new Date(resultModel.getResultTimestamp().toEpochMillisLong()));
        riskValue.setComputeNodeId(_computeNodeId);
        successes.add(riskValue.toSqlParameterSource());
      }
    }
    
    if (successes.isEmpty()) {
      s_logger.info("Nothing to write to DB for {}", resultModel);
      return;
    }
    
    TransactionStatus transaction = getTransactionManager().getTransaction(new DefaultTransactionDefinition());
    try {
      
      insertRows("risk", RiskValue.sqlInsertRisk(), successes);

      getTransactionManager().commit(transaction);
    } catch (RuntimeException e) {
      getTransactionManager().rollback(transaction);
      throw e;
    }

  }

}
