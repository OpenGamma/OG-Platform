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

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInputsImpl;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.ViewComputationCache;
import com.opengamma.util.ArgumentChecker;

/**
 * The job that will actually invoke a {@link FunctionDefinition} as part
 * of dependency graph execution.
 *
 * @author kirk
 */
public class FunctionInvocationJob implements Runnable {
  private static final Logger s_logger = LoggerFactory.getLogger(FunctionInvocationJob.class);
  private static final FunctionExecutionContext EXECUTION_CONTEXT = new FunctionExecutionContext() {
  };
  private final String _functionUniqueIdentifier;
  private final Collection<ValueSpecification> _resolvedInputs;
  private final ViewComputationCache _computationCache;
  private final FunctionRepository _functionRepository;
  private final ComputationTarget _target;
  
  public FunctionInvocationJob(
      String functionUniqueIdentifier,
      Collection<ValueSpecification> resolvedInputs,
      ViewComputationCache computationCache,
      FunctionRepository functionRepository,
      ComputationTarget computationTarget) {
    ArgumentChecker.checkNotNull(functionUniqueIdentifier, "Function identifier");
    ArgumentChecker.checkNotNull(resolvedInputs, "Resolved inputs");
    ArgumentChecker.checkNotNull(computationCache, "Computation Cache");
    ArgumentChecker.checkNotNull(functionRepository, "Function repository");
    ArgumentChecker.checkNotNull(computationTarget, "Computation target");
    _functionUniqueIdentifier = functionUniqueIdentifier;
    _resolvedInputs = resolvedInputs;
    _computationCache = computationCache;
    _functionRepository = functionRepository;
    _target = computationTarget;
  }
  
  /**
   * @return the functionUniqueReference
   */
  public String getFunctionUniqueIdentifier() {
    return _functionUniqueIdentifier;
  }

  /**
   * @return the resolvedInputs
   */
  public Collection<ValueSpecification> getResolvedInputs() {
    return _resolvedInputs;
  }

  /**
   * @return the computationCache
   */
  public ViewComputationCache getComputationCache() {
    return _computationCache;
  }
  
  /**
   * @return the functionRepository
   */
  public FunctionRepository getFunctionRepository() {
    return _functionRepository;
  }

  /**
   * @return the target
   */
  public ComputationTarget getTarget() {
    return _target;
  }

  @Override
  public void run() {
    s_logger.debug("Invoking {} on target {}", getFunctionUniqueIdentifier(), getTarget());
    FunctionInvoker invoker = getFunctionRepository().getInvoker(getFunctionUniqueIdentifier());
    if(invoker == null) {
      throw new NullPointerException("Unable to locate " + getFunctionUniqueIdentifier() + " in function repository.");
    }
    
    FunctionInputs functionInputs = assembleInputs();
    
    Set<ComputedValue> results = invoker.execute(EXECUTION_CONTEXT, functionInputs, getTarget());
    cacheResults(results);
  }

  /**
   * @param results
   */
  protected void cacheResults(Set<ComputedValue> results) {
    for(ComputedValue resultValue : results) {
      getComputationCache().putValue(resultValue);
    }
  }
  
  protected FunctionInputs assembleInputs() {
    Collection<ComputedValue> inputs = new HashSet<ComputedValue>();
    for(ValueSpecification inputSpec : getResolvedInputs()) {
      ComputedValue input = getComputationCache().getValue(inputSpec);
      if(input == null) {
        s_logger.info("Not able to execute as missing input {}", inputSpec);
        throw new MissingInputException(inputSpec, getFunctionUniqueIdentifier());
      }
      inputs.add(getComputationCache().getValue(inputSpec));
    }
    FunctionInputs functionInputs = new FunctionInputsImpl(inputs);
    return functionInputs;
  }

}
