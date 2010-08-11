/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInputsImpl;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.ViewComputationCache;
import com.opengamma.engine.view.cache.ViewComputationCacheSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public abstract class AbstractCalculationNode implements CalculationNode {
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractCalculationNode.class);
  private final ViewComputationCacheSource _cacheSource;
  private final FunctionRepository _functionRepository;
  private final FunctionExecutionContext _functionExecutionContext;
  private final ComputationTargetResolver _targetResolver;
  private final ViewProcessorQuerySender _viewProcessorQuerySender;
  private final String _nodeId;


  protected AbstractCalculationNode(
      ViewComputationCacheSource cacheSource,
      FunctionRepository functionRepository,
      FunctionExecutionContext functionExecutionContext,
      ComputationTargetResolver targetResolver, 
      ViewProcessorQuerySender calcNodeQuerySender,
      String nodeId) {
    ArgumentChecker.notNull(cacheSource, "Cache Source");
    ArgumentChecker.notNull(functionRepository, "Function Repository");
    ArgumentChecker.notNull(functionExecutionContext, "Function Execution Context");
    ArgumentChecker.notNull(targetResolver, "Target Resolver");
    ArgumentChecker.notNull(calcNodeQuerySender, "Calc Node Query Sender");
    ArgumentChecker.notNull(nodeId, "Calculation node ID");

    _cacheSource = cacheSource;
    _functionRepository = functionRepository;
    _functionExecutionContext = functionExecutionContext;
    _targetResolver = targetResolver;
    _viewProcessorQuerySender = calcNodeQuerySender;
    _nodeId = nodeId;
  }

  public ViewComputationCacheSource getCacheSource() {
    return _cacheSource;
  }

  public FunctionRepository getFunctionRepository() {
    return _functionRepository;
  }
  
  public FunctionExecutionContext getFunctionExecutionContext() {
    return _functionExecutionContext;
  }

  public ComputationTargetResolver getTargetResolver() {
    return _targetResolver;
  }
  
  protected ViewProcessorQuerySender getViewProcessorQuerySender() {
    return _viewProcessorQuerySender;
  }
  
  @Override
  public String getNodeId() {
    return _nodeId;
  }

  public synchronized CalculationJobResult executeJob(CalculationJob job) {
    s_logger.info("Executing {}", job);
    
    CalculationJobSpecification spec = job.getSpecification();
    
    // Will not work when multiple functions are being executed in parallel, therefore added synchronized above
    getFunctionExecutionContext().setViewProcessorQuery(new ViewProcessorQuery(getViewProcessorQuerySender(), spec));
    getFunctionExecutionContext().setSnapshotEpochTime(spec.getIterationTimestamp());
    getFunctionExecutionContext().setSnapshotClock(DateUtil.epochFixedClockUTC(spec.getIterationTimestamp()));

    ViewComputationCache cache = getCache(spec);
    
    long startNanos = System.nanoTime();

    List<CalculationJobResultItem> resultItems = new ArrayList<CalculationJobResultItem>();
    
    for (CalculationJobItem jobItem : job.getJobItems()) {
      
      CalculationJobResultItem resultItem;
      try {
        Set<ComputedValue> result = invoke(jobItem, cache);
        cacheResults(cache, result);
        
        resultItem = new CalculationJobResultItem(jobItem);
      
      } catch (MissingInputException e) {
        // NOTE kirk 2009-10-20 -- We intentionally only do the message here so that we don't
        // litter the logs with stack traces.
        s_logger.info("Unable to invoke {} due to missing inputs: {}", jobItem, e.getMessage());
        resultItem = new CalculationJobResultItem(jobItem, e);
      
      } catch (Exception e) {
        s_logger.info("Invoking " + jobItem.getFunctionUniqueIdentifier() + " threw exception.", e);
        resultItem =  new CalculationJobResultItem(jobItem, e);
      }
      
      resultItems.add(resultItem);
    }
    
    long endNanos = System.nanoTime();
    long durationNanos = endNanos - startNanos;
    CalculationJobResult jobResult = new CalculationJobResult(spec, 
        durationNanos, 
        resultItems,
        getNodeId());
    
    s_logger.info("Executed {}", job);
    
    return jobResult;
  }

  @Override
  public ViewComputationCache getCache(CalculationJobSpecification spec) {
    ViewComputationCache cache = getCacheSource().getCache(spec.getViewName(), spec.getCalcConfigName(), spec.getIterationTimestamp());
    return cache;
  }
  
  private Set<ComputedValue> invoke(CalculationJobItem jobItem, ViewComputationCache cache) {
    
    String functionUniqueId = jobItem.getFunctionUniqueIdentifier();

    ComputationTarget target = getTargetResolver().resolve(jobItem.getComputationTargetSpecification());
    if (target == null) {
      throw new OpenGammaRuntimeException("Unable to resolve specification " + jobItem.getComputationTargetSpecification());
    }

    s_logger.debug("Invoking {} on target {}", functionUniqueId, target);
    
    FunctionInvoker invoker = getFunctionRepository().getInvoker(functionUniqueId);
    if (invoker == null) {
      throw new NullPointerException("Unable to locate " + functionUniqueId + " in function repository.");
    }
    
    // assemble inputs
    Collection<ComputedValue> inputs = new HashSet<ComputedValue>();
    Collection<ValueSpecification> missingInputs = new HashSet<ValueSpecification>();
    for (ValueSpecification inputSpec : jobItem.getInputs()) {
      Object input = cache.getValue(inputSpec);
      if (input == null || input instanceof MissingInput) {
        missingInputs.add(inputSpec);
      } else {
        inputs.add(new ComputedValue(inputSpec, input));
      }
    }
    
    if (!missingInputs.isEmpty()) {
      s_logger.info("Not able to execute as missing inputs {}", missingInputs);
      throw new MissingInputException(missingInputs, functionUniqueId);
    }
    
    FunctionInputs functionInputs = new FunctionInputsImpl(inputs);
    
    Set<ComputedValue> results = invoker.execute(getFunctionExecutionContext(), functionInputs, target, jobItem.getDesiredValues());
    return results;
  }
  
  protected void cacheResults(ViewComputationCache cache, Set<ComputedValue> results) {
    for (ComputedValue resultValue : results) {
      cache.putValue(resultValue);
    }
  }
  
}
