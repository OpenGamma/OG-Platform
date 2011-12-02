/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.calc.ResultWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFilter;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ResultModelDefinition;
import com.opengamma.engine.view.ResultOutputMode;
import com.opengamma.engine.view.cache.ViewComputationCache;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.CalculationJobResultItem;
import com.opengamma.engine.view.calcnode.InvocationResult;
import com.opengamma.engine.view.calcnode.MissingInput;
import com.opengamma.financial.conversion.ResultConverter;
import com.opengamma.financial.conversion.ResultConverterCache;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;

/**
 * This writer is used to write risk that originates from a command line batch job. 
 * <p>
 * This writer keeps track of calculation success/failure at a computation target 
 * level. For example, if trade XYZ produces 1,000 risk figures, and 999 succeed
 * and 1 fails, then trade XYZ fails. See table {@code rsk_run_status} in the database.
 * <p>
 * Because of this, clients of this writer MUST collect
 * all results pertaining to a single computation target together and then call 
 * {@link com.opengamma.engine.view.calc.ResultWriter#write(CalculationJobResult, DependencyGraph)}
 * with the entire set of results for that computation target.
 * <p>
 * A call to
 * {@link com.opengamma.engine.view.calc.ResultWriter#write(CalculationJobResult, DependencyGraph)}
 * can include results for multiple computation targets, as long as it
 * is still true that results for the <i>same</i> target are not scattered across
 * multiple calls. 
 * <p>
 * {@link DbBatchMaster#createDependencyGraphExecutorFactory(com.opengamma.financial.batch.BatchJobRun)} 
 * shows how to guarantee this in practice by using {@link com.opengamma.engine.view.calc.BatchExecutor}.
 *  
 */
public class CommandLineBatchResultWriter extends AbstractBatchResultWriter implements ResultWriter {
  
  private static final Logger s_logger = LoggerFactory.getLogger(CommandLineBatchResultWriter.class);
  
  /**
   * Used to decide what risk to write into DB
   */
  private final ResultModelDefinition _resultModelDefinition;
  
  /**
   * Caches
   */
  private final Map<String, ViewComputationCache> _cachesByCalculationConfiguration;
  
  /**
   * Used to determine whether it's worth checking the status
   * table for already-executed entries. If this is the first
   * time we're running the batch, there won't be anything in 
   * the status table, so it's not necessary to make queries
   * against it.
   */
  private volatile boolean _isRestart;
  
  /**
   * It is possible to disable writing errors into
   * 
   * rsk_compute_failure  
   * rsk_failure 
   * rsk_failure_reason
   * 
   * by setting this to false.
   * 
   */
  private final boolean _writeErrors = true;
  
  public CommandLineBatchResultWriter(DbConnector dbConnector,
      ResultModelDefinition resultModelDefinition,
      Map<String, ViewComputationCache> cachesByCalculationConfiguration,
      Set<ComputationTarget> computationTargets,
      RiskRun riskRun,
      Set<RiskValueName> valueNames,
      Set<RiskValueRequirement> valueRequirements,
      Set<RiskValueSpecification> valueSpecifications) {
    this(dbConnector,
        resultModelDefinition,
        cachesByCalculationConfiguration,
        computationTargets,
        riskRun,
        valueNames,
        valueRequirements,
        valueSpecifications,
        new ResultConverterCache());
  }
  
  public CommandLineBatchResultWriter(
      DbConnector dbConnector,
      ResultModelDefinition resultModelDefinition,
      Map<String, ViewComputationCache> cachesByCalculationConfiguration,
      Set<ComputationTarget> computationTargets,
      RiskRun riskRun,
      Set<RiskValueName> valueNames,
      Set<RiskValueRequirement> valueRequirements,
      Set<RiskValueSpecification> valueSpecifications,
      ResultConverterCache resultConverterCache) {

    super(dbConnector, riskRun, resultConverterCache, computationTargets, valueNames, valueRequirements, valueSpecifications);

    ArgumentChecker.notNull(resultModelDefinition, "Result model definition");
    ArgumentChecker.notNull(cachesByCalculationConfiguration, "Caches by calculation configuration");
    
    _resultModelDefinition = resultModelDefinition;
    _cachesByCalculationConfiguration = cachesByCalculationConfiguration;
    
    setRestart(riskRun.isRestart());
  }
  
  public boolean isWriteErrors() {
    return _writeErrors;
  }

  public boolean isRestart() {
    return _isRestart;
  }
  
  public void setRestart(boolean isRestart) {
    _isRestart = isRestart;
  }

  // --------------------------------------------------------------------------
  
  @Override
  public DependencyGraph getGraphToExecute(final DependencyGraph graph) {
    if (!isRestart()) {
      // First time around, always execute everything.
      return graph;      
    }

    // The batch has been restarted. Figure out from the status table and the computation
    // cache what needs to be recomputed.
    
    DependencyGraph subGraph = graph.subGraph(new DependencyNodeFilter() {
      @Override
      public boolean accept(DependencyNode node) {
        return shouldExecute(graph, node);
      }
    });
    return subGraph;
  }
  
  private boolean shouldExecute(DependencyGraph graph, DependencyNode node) {
    
    ViewComputationCache cache = getCache(graph.getCalculationConfigurationName());
     
    if (_resultModelDefinition.shouldOutputFromNode(node)) {
      // e.g., POSITIONS and PORTFOLIOS
      StatusEntry.Status status = getStatus(graph.getCalculationConfigurationName(), node.getComputationTarget().toSpecification());
      switch (status) {
        case SUCCESS:
          if (allOutputsInCache(node, cache)) {
            return false;
          } else {
            return true;
          }

        case NOT_RUNNING:
        case RUNNING:
        case FAILURE:
          return true;
        
        default:
          throw new RuntimeException("Unexpected status " + status);
      }
    } else {
      // e.g., PRIMITIVES. If the computation cache has been re-started along with the 
      // batch, it is necessary to re-evaluate the item, but not otherwise.
      if (allOutputsInCache(node, cache)) {
        return false; 
      } else {
        return true;
      }
    }
  }
  
  // --------------------------------------------------------------------------

  @Override
  public synchronized void write(CalculationJobResult result, DependencyGraph depGraph) {
    if (result.getResultItems().isEmpty()) {
      s_logger.info("{}: Nothing to insert into DB", result);
      return;
    }
    
    if (!isInitialized()) {
      initialize();
    }
    
    ViewComputationCache cache = getCache(result);
    
    try {
      getSessionFactory().getCurrentSession().beginTransaction();
      
      joinSession();
      
      writeImpl(cache, result, depGraph);
    
      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }
  }
  
  private void writeImpl(ViewComputationCache cache, CalculationJobResult result, DependencyGraph depGraph) {
    
    // STAGE 1. Populate error information in the shared computation cache.
    // This is done for all items and will populate table rsk_compute_failure. 
    for (CalculationJobResultItem item : result.getResultItems()) {
      populateErrorCache(cache, item);
    }
    
    // STAGE 2. Work out which targets:
    // 1) succeeded and should be written into rsk_value (because ALL items for that target succeeded)
    // 2) failed and should be written into rsk_failure (because AT LEAST ONE item for that target failed)
    
    Set<ComputationTargetSpecification> successfulTargets = new HashSet<ComputationTargetSpecification>();
    Set<ComputationTargetSpecification> failedTargets = new HashSet<ComputationTargetSpecification>();
    
    for (CalculationJobResultItem item : result.getResultItems()) {
      ResultOutputMode targetOutputMode = _resultModelDefinition.getOutputMode(item.getComputationTargetSpecification().getType());
      if (targetOutputMode == ResultOutputMode.NONE) {
        // Any sort of output is disabled for this target type
        continue;
      }

      ComputationTargetSpecification target = item.getComputationTargetSpecification();
      
      boolean success; 
      
      if (item.getResult() == InvocationResult.SUCCESS) {
        success = !failedTargets.contains(target);
        
        if (success) {
          // also check output types
          for (ValueSpecification output : item.getOutputs()) {
            Object value = cache.getValue(output);
            if (value == null) {
              s_logger.error("Cache not populated for item " + item 
                  + ", output " + output);
              success = false;
              break;
            }
            
            try {
              getResultConverterCache().getConverter(value);
            } catch (IllegalArgumentException e) {
              s_logger.error("Cannot insert value of type " + value.getClass() + " for " + item, e);
              success = false;
              break;
            }
          }
        }
      } else {
        success = false;
      }
      
      if (success) {
        successfulTargets.add(target);        
      } else {
        successfulTargets.remove(target);
        failedTargets.add(target);
      }
    
    }
    
    List<SqlParameterSource> successes = new ArrayList<SqlParameterSource>();
    List<SqlParameterSource> failures = new ArrayList<SqlParameterSource>();
    List<SqlParameterSource> failureReasons = new ArrayList<SqlParameterSource>();
    
    int riskRunId = getRiskRunId();
    int calcConfId = getCalculationConfigurationId(result.getSpecification().getCalcConfigName());
    int computeNodeId = getComputeNodeId(result.getComputeNodeId());
    
    Date evalInstant = new Date();
    
    // STAGE 3. Based on the results of stage 2, work out 
    // SQL statements to write risk into rsk_value and rsk_failure (& rsk_failure_reason)
    
    for (CalculationJobResultItem item : result.getResultItems()) {

      ResultOutputMode targetOutputMode = _resultModelDefinition.getOutputMode(item.getComputationTargetSpecification().getType());
      
      if (successfulTargets.contains(item.getComputationTargetSpecification())) {
        
        // make sure the values are not already in db, don't want to insert twice
        StatusEntry.Status status = getStatus(
            result.getSpecification().getCalcConfigName(), 
            item.getComputationTargetSpecification());
        if (status == StatusEntry.Status.SUCCESS) {
          continue;
        }

        for (ValueSpecification output : item.getOutputs()) {
          if (!targetOutputMode.shouldOutputResult(output, depGraph)) {
            continue;
          }
          
          Object outputValue = cache.getValue(output);
          @SuppressWarnings("unchecked")
          ResultConverter<Object> resultConverter = (ResultConverter<Object>) getResultConverterCache().getConverter(outputValue);
          Map<String, Double> valuesAsDoubles = resultConverter.convert(output.getValueName(), outputValue);
          
          int computationTargetId = getComputationTargetId(output.getTargetSpecification());

          for (Map.Entry<String, Double> riskValueEntry : valuesAsDoubles.entrySet()) {
            for (ValueRequirement requirement : item.getItem().getDesiredValues()) {
              int valueRequirementId = getValueRequirementId(requirement.getConstraints());
              int valueSpecificationId = getValueSpecificationId(output.getProperties());
              int valueNameId = getValueNameId(riskValueEntry.getKey());
              int functionUniqueId = getFunctionUniqueId(output.getFunctionUniqueId());

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
              riskValue.setEvalInstant(evalInstant);
              riskValue.setComputeNodeId(computeNodeId);
              successes.add(riskValue.toSqlParameterSource());
            }
          }
        }
        
      // the check below ensures that
      // if there is a partial failure (some successes, some failures) for a target, 
      // only the failures will be written out in the database
      } else if (failedTargets.contains(item.getComputationTargetSpecification())) {
          
        if (!isWriteErrors()) {
          continue;
        }
        
        for (ValueSpecification outputValue : item.getOutputs()) {
          for (ValueRequirement requirement : item.getItem().getDesiredValues()) {
            int valueNameId = getValueNameId(outputValue.getValueName());
            int functionUniqueId = getFunctionUniqueId(outputValue.getFunctionUniqueId());
            int computationTargetId = getComputationTargetId(outputValue.getTargetSpecification());
            int valueRequirementId = getValueRequirementId(requirement.getConstraints());
            int valueSpecificationId = getValueSpecificationId(outputValue.getProperties());

            RiskFailure failure = new RiskFailure();
            failure.setId(generateUniqueId());
            failure.setCalculationConfigurationId(calcConfId);
            failure.setValueNameId(valueNameId);
            failure.setValueRequirementId(valueRequirementId);
            failure.setValueSpecificationId(valueSpecificationId);
            failure.setFunctionUniqueId(functionUniqueId);
            failure.setComputationTargetId(computationTargetId);
            failure.setRunId(riskRunId);
            failure.setEvalInstant(evalInstant);
            failure.setComputeNodeId(computeNodeId);
            failures.add(failure.toSqlParameterSource());

            switch (item.getResult()) {

              case MISSING_INPUTS:
              case FUNCTION_THREW_EXCEPTION:

                BatchResultWriterFailure cachedFailure = (BatchResultWriterFailure) cache.getValue(outputValue);
                if (cachedFailure != null) {
                  for (Number computeFailureId : cachedFailure.getComputeFailureIds()) {
                    FailureReason reason = new FailureReason();
                    reason.setId(generateUniqueId());
                    reason.setRiskFailure(failure);
                    reason.setComputeFailureId(computeFailureId.longValue());
                    failureReasons.add(reason.toSqlParameterSource());
                  }
                }

                break;

              case SUCCESS:

                // maybe this output succeeded, but some other outputs for the same target failed.
                s_logger.debug("Not adding any failure reasons for partial failures / unsupported outputs for now");
                break;

              default:
                throw new RuntimeException("Should not get here");
            }
          }
        }
          
      } else {
        // probably a PRIMITIVE target. See targetOutputMode == ResultOutputMode.NONE check above.
        s_logger.debug("Not writing anything for target {}", item.getComputationTargetSpecification());
      }
    }
    
    // STAGE 4. Actually execute the statements worked out in stage 3.
    
    if (successes.isEmpty() 
        && failures.isEmpty() 
        && failureReasons.isEmpty() 
        && successfulTargets.isEmpty() 
        && failedTargets.isEmpty()) {
      s_logger.debug("Nothing to write to DB for {}", result);
      return;
    }
    
    // need to figure out why these 2 lines are needed for the insertRows() code not to block...
    // this is bad - loss of transactionality...
    getSessionFactory().getCurrentSession().getTransaction().commit();
    getSessionFactory().getCurrentSession().beginTransaction();
    
    insertRows("risk", RiskValue.sqlInsertRisk(), successes);
    insertRows("risk failure", RiskFailure.sqlInsertRiskFailure(), failures);
    insertRows("risk failure reason", FailureReason.sqlInsertRiskFailureReason(), failureReasons);
      
    upsertStatusEntries(result.getSpecification(), StatusEntry.Status.SUCCESS, successfulTargets);
    upsertStatusEntries(result.getSpecification(), StatusEntry.Status.FAILURE, failedTargets);
  }
  
  // --------------------------------------------------------------------------

  private void populateErrorCache(ViewComputationCache cache, CalculationJobResultItem item) {
    BatchResultWriterFailure cachedFailure = new BatchResultWriterFailure();
    
    switch (item.getResult()) {

      case FUNCTION_THREW_EXCEPTION:
      
        // an "original" failure
        //
        // There will only be 1 failure reason.
        
        ComputeFailure computeFailure = getComputeFailureFromDb(item);
        cachedFailure.addComputeFailureId(computeFailure.getId());
        
        break;
      
      case MISSING_INPUTS:
        
        // There may be 1-N failure reasons - one for each failed
        // function in the subtree below this node. (This
        // only includes "original", i.e., lowest-level, failures.)
        
        for (ValueSpecification missingInput : item.getMissingInputs()) {
          BatchResultWriterFailure inputFailure = (BatchResultWriterFailure) cache.getValue(missingInput);
          
          if (inputFailure == null) {

            ComputeFailureKey computeFailureKey = new ComputeFailureKey(
                missingInput.getFunctionUniqueId(),
                "N/A",
                "Missing input " + missingInput,
                "N/A");
            computeFailure = getComputeFailureFromDb(computeFailureKey);
            cachedFailure.addComputeFailureId(computeFailure.getId());

          } else {
            
            cachedFailure.addComputeFailureIds(inputFailure.getComputeFailureIds());
          
          }
        }
        
        break;
    }
    
    if (!cachedFailure.getComputeFailureIds().isEmpty()) {
      for (ValueSpecification outputValue : item.getOutputs()) {
        cache.putSharedValue(new ComputedValue(outputValue, cachedFailure));
      }
    }
  }
  
  /*package*/ ComputeFailure getComputeFailureFromDb(CalculationJobResultItem item) {
    if (item.getResult() != InvocationResult.FUNCTION_THREW_EXCEPTION) {
      throw new IllegalArgumentException("Please give a failed item");       
    }
    
    ComputeFailureKey computeFailureKey = new ComputeFailureKey(
        item.getItem().getFunctionUniqueIdentifier(),
        item.getExceptionClass(),
        item.getExceptionMsg(),
        item.getStackTrace());
    return getComputeFailureFromDb(computeFailureKey);
  }

  /**
   * Instances of this class are saved in the computation cache for each
   * failure (whether the failure is 'original' or due to missing inputs).
   * The set of Longs is a set of compute failure IDs (referencing
   * rsk_compute_failure(id)). The set is built bottom up. 
   * For example, if A has two children, B and C, and B has failed
   * due to error 12, and C has failed due to errors 15 and 16, then
   * A has failed due to errors 12, 15, and 16.
   */
  public static class BatchResultWriterFailure implements MissingInput, Serializable {
    /** Serialization version. */
    private static final long serialVersionUID = 1L;
    private Set<Number> _computeFailureIds = new HashSet<Number>();

    public Set<Number> getComputeFailureIds() {
      return Collections.unmodifiableSet(_computeFailureIds);
    }
    
    public void setComputeFailureIds(Set<Number> computeFailureIds) {
      _computeFailureIds = computeFailureIds;
    }

    public void addComputeFailureId(Number computeFailureId) {
      addComputeFailureIds(Collections.singleton(computeFailureId));
    }

    public void addComputeFailureIds(Set<? extends Number> computeFailureIds) {
      _computeFailureIds.addAll(computeFailureIds);
    }
  }
  
  // --------------------------------------------------------------------------
  
  public ViewComputationCache getCache(String calcConf) {
    ViewComputationCache cache = _cachesByCalculationConfiguration.get(calcConf);
    if (cache == null) {
      throw new IllegalArgumentException("There is no cache for calc conf " + calcConf);
    }
    return cache;
  }
  
  public ViewComputationCache getCache(CalculationJobResult result) {
    return getCache(result.getSpecification().getCalcConfigName());
  }
  
  private boolean allOutputsInCache(DependencyNode node, ViewComputationCache cache) {
    boolean allOutputsInCache = true;
    
    for (ValueSpecification output : node.getOutputValues()) {
      if (cache.getValue(output) == null) {
        allOutputsInCache = false;
        break;
      }
    }
    
    return allOutputsInCache;
  }
  
}
