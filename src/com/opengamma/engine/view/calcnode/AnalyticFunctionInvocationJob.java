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
    Collection<AnalyticValue<?>> inputs = new HashSet<AnalyticValue<?>>();
    for(AnalyticValueDefinition<?> inputDefinition : getResolvedInputs()) {
      inputs.add(getComputationCache().getValue(inputDefinition));
    }
    AnalyticFunctionInputs functionInputs = new AnalyticFunctionInputsImpl(inputs);
    
    AnalyticFunctionInvoker invoker = getFunctionRepository().getInvoker(getFunctionUniqueIdentifier());
    
    Collection<AnalyticValue<?>> outputs = invoker.execute(functionInputs, getSecurity());
    for(AnalyticValue<?> outputValue : outputs) {
      getComputationCache().putValue(outputValue);
    }
  }

}
