/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

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
import com.opengamma.engine.analytics.LiveDataSourcingFunction;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.security.Security;

/**
 * The job that will actually invoke a {@link AnalyticFunctionDefinition} as part
 * of dependency graph execution.
 *
 * @author kirk
 */
public class AnalyticFunctionInvocationJob implements Runnable {
  private static final Logger s_logger = LoggerFactory.getLogger(AnalyticFunctionInvocationJob.class);
  private final DependencyNode _node;
  private final Security _security;
  private final ViewComputationCache _computationCache;
  private final AnalyticFunctionRepository _functionRepository;
  
  public AnalyticFunctionInvocationJob(
      DependencyNode node,
      Security security,
      ViewComputationCache computationCache,
      AnalyticFunctionRepository functionRepository) {
    assert node != null;
    assert security != null;
    assert computationCache != null;
    assert functionRepository != null;
    _node = node;
    _security = security;
    _computationCache = computationCache;
    _functionRepository = functionRepository;
  }

  /**
   * @return the node
   */
  public DependencyNode getNode() {
    return _node;
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
    // First of all, check that we don't have the outputs already ready.
    boolean allFound = true;
    for(AnalyticValueDefinition<?> outputDefinition : getNode().getOutputValues()) {
      if(getComputationCache().getValue(outputDefinition) == null) {
        allFound = false;
        break;
      }
    }
    
    if(allFound) {
      if(!(getNode().getFunction() instanceof LiveDataSourcingFunction)) {
        s_logger.debug("Able to skip a node because it was already computed.");
      }
      return;
    }
    
    Collection<AnalyticValue<?>> inputs = new HashSet<AnalyticValue<?>>();
    for(AnalyticValueDefinition<?> inputDefinition : getNode().getInputValues()) {
      AnalyticValueDefinition<?> resolvedDefinition = getNode().getResolvedInput(inputDefinition);
      inputs.add(getComputationCache().getValue(resolvedDefinition));
    }
    AnalyticFunctionInputs functionInputs = new AnalyticFunctionInputsImpl(inputs);
    
    AnalyticFunctionInvoker invoker = getFunctionRepository().getInvoker(getNode().getFunction().getUniqueIdentifier());
    
    Collection<AnalyticValue<?>> outputs = invoker.execute(functionInputs, getSecurity());
    for(AnalyticValue<?> outputValue : outputs) {
      getComputationCache().putValue(outputValue);
    }
  }

}
