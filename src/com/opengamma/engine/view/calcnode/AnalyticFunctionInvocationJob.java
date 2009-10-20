/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.Collection;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.analytics.AnalyticFunctionDefinition;
import com.opengamma.engine.analytics.AnalyticFunctionInputs;
import com.opengamma.engine.analytics.AnalyticFunctionInvoker;
import com.opengamma.engine.analytics.AnalyticFunctionRepository;
import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.analytics.FunctionExecutionContext;
import com.opengamma.engine.analytics.PrimitiveAnalyticFunctionInvoker;
import com.opengamma.engine.analytics.SecurityAnalyticFunctionInvoker;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.view.AnalyticFunctionInputsImpl;
import com.opengamma.engine.view.ViewComputationCache;

/**
 * The job that will actually invoke a {@link AnalyticFunctionDefinition} as part
 * of dependency graph execution.
 *
 * @author kirk
 */
public class AnalyticFunctionInvocationJob implements Runnable {
  private static final Logger s_logger = LoggerFactory.getLogger(AnalyticFunctionInvocationJob.class);
  private static final FunctionExecutionContext EXECUTION_CONTEXT = new FunctionExecutionContext() {
  };
  private final String _functionUniqueIdentifier;
  private final Collection<AnalyticValueDefinition<?>> _resolvedInputs;
  private final Security _security;
  private final ViewComputationCache _computationCache;
  private final AnalyticFunctionRepository _functionRepository;
  
  public AnalyticFunctionInvocationJob(
      String functionUniqueIdentifier,
      Collection<AnalyticValueDefinition<?>> resolvedInputs,
      Security security,
      ViewComputationCache computationCache,
      AnalyticFunctionRepository functionRepository) {
    assert functionUniqueIdentifier != null;
    assert resolvedInputs != null;
    assert security != null;
    assert computationCache != null;
    assert functionRepository != null;
    _functionUniqueIdentifier = functionUniqueIdentifier;
    _resolvedInputs = resolvedInputs;
    _security = security;
    _computationCache = computationCache;
    _functionRepository = functionRepository;
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
  public Collection<AnalyticValueDefinition<?>> getResolvedInputs() {
    return _resolvedInputs;
  }

  /**
   * @return the computationCache
   */
  public ViewComputationCache getComputationCache() {
    return _computationCache;
  }

  /**
   * @return the security
   */
  public Security getSecurity() {
    return _security;
  }

  /**
   * @return the functionRepository
   */
  public AnalyticFunctionRepository getFunctionRepository() {
    return _functionRepository;
  }

  @Override
  public void run() {
    s_logger.debug("Invoking {} on security {}", getFunctionUniqueIdentifier(), getSecurity());
    AnalyticFunctionInvoker invoker = getFunctionRepository().getInvoker(getFunctionUniqueIdentifier());
    if(invoker == null) {
      throw new NullPointerException("Unable to locate " + getFunctionUniqueIdentifier() + " in function repository.");
    }
    
    Collection<AnalyticValue<?>> inputs = new HashSet<AnalyticValue<?>>();
    for(AnalyticValueDefinition<?> inputDefinition : getResolvedInputs()) {
      AnalyticValue<?> input = getComputationCache().getValue(inputDefinition);
      if(input == null) {
        s_logger.info("Not able to execute as missing input {}", inputDefinition);
        throw new MissingInputException(inputDefinition, getFunctionUniqueIdentifier());
      }
      inputs.add(getComputationCache().getValue(inputDefinition));
    }
    AnalyticFunctionInputs functionInputs = new AnalyticFunctionInputsImpl(inputs);
    
    Collection<AnalyticValue<?>> outputs = null;
    if(invoker instanceof PrimitiveAnalyticFunctionInvoker) {
      outputs = ((PrimitiveAnalyticFunctionInvoker) invoker).execute(EXECUTION_CONTEXT, functionInputs);
    } else if(invoker instanceof SecurityAnalyticFunctionInvoker) {
      outputs = ((SecurityAnalyticFunctionInvoker) invoker).execute(EXECUTION_CONTEXT, functionInputs, getSecurity());
    } else {
      throw new UnsupportedOperationException("Only primitive and security invokers supported now.");
    }
    for(AnalyticValue<?> outputValue : outputs) {
      getComputationCache().putValue(outputValue);
    }
  }

}
