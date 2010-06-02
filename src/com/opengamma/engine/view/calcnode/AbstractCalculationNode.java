/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionRepository;
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
    assert spec != null;
    ViewComputationCache cache = getCacheSource().getCache(spec.getViewName(), spec.getCalcConfigName(), spec.getIterationTimestamp());
    ComputationTarget target = getTargetResolver().resolve(job.getComputationTargetSpecification());
    if (target == null) {
      throw new OpenGammaRuntimeException("Unable to resolve specification " + job.getComputationTargetSpecification());
    }
    FunctionInvocationJob invocationJob = new FunctionInvocationJob(spec, job.getFunctionUniqueIdentifier(), job.getInputs(), cache,
                                                                    getFunctionRepository(), getFunctionExecutionContext(), 
                                                                    new ViewProcessorQuery(getViewProcessorQuerySender(), spec),
                                                                    target, job.getDesiredValues());
    long startNanos = System.nanoTime();
    boolean wasException = false;
    try {
      invocationJob.run();
    } catch (MissingInputException e) {
      // NOTE kirk 2009-10-20 -- We intentionally only do the message here so that we don't
      // litter the logs with stack traces.
      s_logger.info("Unable to invoke due to missing inputs invoking on {}: {}", target, e.getMessage());
      wasException = true;
    } catch (Exception e) {
      s_logger.info("Invoking " + job.getFunctionUniqueIdentifier() + " on " + target + " throw exception.", e);
      wasException = true;
    }
    long endNanos = System.nanoTime();
    long durationNanos = endNanos - startNanos;
    InvocationResult invocationResult = wasException ? InvocationResult.ERROR : InvocationResult.SUCCESS;
    CalculationJobResult jobResult = new CalculationJobResult(spec, invocationResult, durationNanos);
    return jobResult;
  }

}
