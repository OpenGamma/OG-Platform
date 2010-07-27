/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtil;
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

/**
 * 
 */
public abstract class AbstractCalculationNode {
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractCalculationNode.class);
  private final ViewComputationCacheSource _cacheSource;
  private final FunctionRepository _functionRepository;
  private final FunctionExecutionContext _functionExecutionContext;
  private final ComputationTargetResolver _targetResolver;
  private final ViewProcessorQuerySender _viewProcessorQuerySender;



  protected AbstractCalculationNode(
      ViewComputationCacheSource cacheSource,
      FunctionRepository functionRepository,
      FunctionExecutionContext functionExecutionContext,
      ComputationTargetResolver targetResolver, 
      ViewProcessorQuerySender calcNodeQuerySender) {
    ArgumentChecker.notNull(cacheSource, "Cache Source");
    ArgumentChecker.notNull(functionRepository, "Function Repository");
    ArgumentChecker.notNull(functionExecutionContext, "Function Execution Context");
    ArgumentChecker.notNull(targetResolver, "Target Resolver");
    ArgumentChecker.notNull(calcNodeQuerySender, "Calc Node Query Sender");
    _cacheSource = cacheSource;
    _functionRepository = functionRepository;
    _functionExecutionContext = functionExecutionContext;
    _targetResolver = targetResolver;
    _viewProcessorQuerySender = calcNodeQuerySender;
  }

  /**
   * @return the cacheSource
   */
  public ViewComputationCacheSource getCacheSource() {
    return _cacheSource;
  }

  /**
   * @return the functionRepository
   */
  public FunctionRepository getFunctionRepository() {
    return _functionRepository;
  }
  
  /**
   * @return the function execution context
   */
  public FunctionExecutionContext getFunctionExecutionContext() {
    return _functionExecutionContext;
  }

  /**
   * @return the targetResolver
   */
  public ComputationTargetResolver getTargetResolver() {
    return _targetResolver;
  }
  
  /**
   * @return the calcNodeQuerySender
   */
  protected ViewProcessorQuerySender getViewProcessorQuerySender() {
    return _viewProcessorQuerySender;
  }

  protected CalculationJobResult executeJob(CalculationJob job) {
    CalculationJobSpecification spec = job.getSpecification();
    
    // BUG - will not work when multiple functions are being executed in parallel
    getFunctionExecutionContext().setViewProcessorQuery(new ViewProcessorQuery(getViewProcessorQuerySender(), spec));
    getFunctionExecutionContext().setSnapshotEpochTime(spec.getIterationTimestamp());
    getFunctionExecutionContext().setSnapshotClock(DateUtil.epochFixedClockUTC(spec.getIterationTimestamp()));

    ViewComputationCache cache = getCacheSource().getCache(spec.getViewName(), spec.getCalcConfigName(), spec.getIterationTimestamp());
    
    long startNanos = System.nanoTime();
    Exception exception = null;

    Set<ComputedValue> results = new HashSet<ComputedValue>();
    for (CalculationJobItem jobItem : job.getJobItems()) {
    
      try {
        invoke(jobItem, cache);
      
      } catch (MissingInputException e) {
        // NOTE kirk 2009-10-20 -- We intentionally only do the message here so that we don't
        // litter the logs with stack traces.
        s_logger.info("Unable to invoke {} due to missing inputs: {}", jobItem, e.getMessage());
        exception = e;
        break;
      
      } catch (Exception e) {
        s_logger.info("Invoking " + jobItem.getFunctionUniqueIdentifier() + " threw exception.", e);
        exception = e;
        break;
      }
    }
    
    cacheResults(cache, results);
    
    long endNanos = System.nanoTime();
    long durationNanos = endNanos - startNanos;
    InvocationResult invocationResult = (exception == null) ? InvocationResult.SUCCESS : InvocationResult.ERROR;

    CalculationJobResult jobResult = new CalculationJobResult(spec, invocationResult, durationNanos);
    return jobResult;
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
    for (ValueSpecification inputSpec : jobItem.getInputs()) {
      Object input = cache.getValue(inputSpec);
      if (input == null) {
        s_logger.info("Not able to execute as missing input {}", inputSpec);
        throw new MissingInputException(inputSpec, functionUniqueId);
      }
      inputs.add(new ComputedValue(inputSpec, input));
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
