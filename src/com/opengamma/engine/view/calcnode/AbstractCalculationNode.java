/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.view.cache.ViewComputationCache;
import com.opengamma.engine.view.cache.ViewComputationCacheSource;

/**
 * 
 *
 * @author kirk
 */
public abstract class AbstractCalculationNode {
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractCalculationNode.class);
  private final ViewComputationCacheSource _cacheSource;
  private final FunctionRepository _functionRepository;
  private final FunctionExecutionContext _functionExecutionContext;
  private final ComputationTargetResolver _targetResolver;

  protected AbstractCalculationNode(
      ViewComputationCacheSource cacheSource,
      FunctionRepository functionRepository,
      FunctionExecutionContext functionExecutionContext,
      ComputationTargetResolver targetResolver) {
    // TODO kirk 2009-09-25 -- Check inputs
    _cacheSource = cacheSource;
    _functionRepository = functionRepository;
    _functionExecutionContext = functionExecutionContext;
    _targetResolver = targetResolver;
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

  protected CalculationJobResult executeJob(CalculationJob job) {
    CalculationJobSpecification spec = job.getSpecification();
    assert spec != null;
    ViewComputationCache cache = getCacheSource().getCache(spec.getViewName(), spec.getCalcConfigName(), spec.getIterationTimestamp());
    ComputationTarget target = getTargetResolver().resolve(job.getComputationTargetSpecification());
    if(target == null) {
      throw new OpenGammaRuntimeException("Unable to resolve specification " + job.getComputationTargetSpecification());
    }
    FunctionInvocationJob invocationJob = new FunctionInvocationJob(job.getFunctionUniqueIdentifier(), job.getInputs(), cache, getFunctionRepository(), getFunctionExecutionContext(), 
                                                                    target, job.getDesiredValues());
    long startTS = System.currentTimeMillis();
    boolean wasException = false;
    try {
      invocationJob.run();
    } catch (MissingInputException e) {
      // NOTE kirk 2009-10-20 -- We intentionally only do the message here so that we don't
      // litter the logs with stack traces.
      s_logger.info("Unable to invoke due to missing inputs invoking on {}: {}", target, e.getMessage());
      wasException = true;
    } catch (Exception e) {
      s_logger.info("Invoking " + job.getFunctionUniqueIdentifier() + " on " + target + " throw exception.",e);
      wasException = true;
    }
    long endTS = System.currentTimeMillis();
    long duration = endTS - startTS;
    InvocationResult invocationResult = wasException ? InvocationResult.ERROR : InvocationResult.SUCCESS;
    CalculationJobResult jobResult = new CalculationJobResult(spec, invocationResult, duration);
    return jobResult;
  }

}
