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

import com.opengamma.engine.function.AggregatePositionFunctionInvoker;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.function.PositionFunctionInvoker;
import com.opengamma.engine.function.PrimitiveFunctionInvoker;
import com.opengamma.engine.function.SecurityFunctionInvoker;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.value.AnalyticValueDefinition;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.view.AnalyticFunctionInputsImpl;
import com.opengamma.engine.view.cache.ViewComputationCache;

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
  private final Collection<AnalyticValueDefinition<?>> _resolvedInputs;
  private final Security _security;
  private final Position _position;
  private final Collection<Position> _positions;
  private final ViewComputationCache _computationCache;
  private final FunctionRepository _functionRepository;
  
  // Primitive function constructor
  public FunctionInvocationJob(
      String functionUniqueIdentifier,
      Collection<AnalyticValueDefinition<?>> resolvedInputs,
      ViewComputationCache computationCache,
      FunctionRepository functionRepository) {
    assert functionUniqueIdentifier != null;
    assert resolvedInputs != null;
    assert computationCache != null;
    assert functionRepository != null;
    _functionUniqueIdentifier = functionUniqueIdentifier;
    _resolvedInputs = resolvedInputs;
    _security = null;
    _position = null;
    _positions = null;
    _computationCache = computationCache;
    _functionRepository = functionRepository;
  }
  
  // Security specific function constructor
  public FunctionInvocationJob(
      String functionUniqueIdentifier,
      Collection<AnalyticValueDefinition<?>> resolvedInputs,
      Security security,
      ViewComputationCache computationCache,
      FunctionRepository functionRepository) {
    assert functionUniqueIdentifier != null;
    assert resolvedInputs != null;
    assert security != null;
    assert computationCache != null;
    assert functionRepository != null;
    _functionUniqueIdentifier = functionUniqueIdentifier;
    _resolvedInputs = resolvedInputs;
    _security = security;
    _position = null;
    _positions = null;
    _computationCache = computationCache;
    _functionRepository = functionRepository;
  }
  
  // Position specific function constructor
  public FunctionInvocationJob(
      String functionUniqueIdentifier,
      Collection<AnalyticValueDefinition<?>> resolvedInputs,
      Position position,
      ViewComputationCache computationCache,
      FunctionRepository functionRepository) {
    assert functionUniqueIdentifier != null;
    assert resolvedInputs != null;
    assert position != null;
    assert computationCache != null;
    assert functionRepository != null;
    _functionUniqueIdentifier = functionUniqueIdentifier;
    _resolvedInputs = resolvedInputs;
    _security = null;
    _position = position;
    _positions = null;
    _computationCache = computationCache;
    _functionRepository = functionRepository;
  }
  
  // Aggregate position function constructor
  public FunctionInvocationJob(
      String functionUniqueIdentifier,
      Collection<AnalyticValueDefinition<?>> resolvedInputs,
      Collection<Position> positions,
      ViewComputationCache computationCache,
      FunctionRepository functionRepository) {
    assert functionUniqueIdentifier != null;
    assert resolvedInputs != null;
    assert computationCache != null;
    assert functionRepository != null;
    _functionUniqueIdentifier = functionUniqueIdentifier;
    _resolvedInputs = resolvedInputs;
    _security = null;
    _position = null;
    _positions = positions;
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
   * This should only be called if getComputationTargetType() returns SECURITY_KEY
   * @return the securityKey
   */
  public Security getSecurity() {
    if (_security == null) {
      s_logger.warn("getSecurityKey() called when job is "+toString());
    }
    return _security;
  }
  
  /**
   * This should only be called if getComputationTargetType() returns POSITION
   * @return the position
   */
  public Position getPosition() {
    if (_position == null) {
      s_logger.warn("getPosition() called when job is "+toString());
    }
    return _position;
  }
  
  /**
   * This should only be called if getPositions() returns AGGREGATE_POSITION
   * @return the positions
   */
  public Collection<Position> getPositions() {
    if (_positions == null) {
      s_logger.warn("getPositions() called when job is "+toString());
    }
    return _positions;
  }
  
  public ComputationTarget getComputationTargetType() {
    if (_security != null) {
      assert _position == null;
      assert _positions == null;
      return ComputationTarget.SECURITY;
    } else if (_position != null) {
      assert _positions == null; // already checked _securityKey
      return ComputationTarget.POSITION;
    } else if (_positions != null) { // already checked the others.
      return ComputationTarget.MULTIPLE_POSITIONS;
    } else {
      return ComputationTarget.PRIMITIVE;
    }
  }
  
  public enum ComputationTarget {
    PRIMITIVE, SECURITY, POSITION, MULTIPLE_POSITIONS
  }

  /**
   * @return the functionRepository
   */
  public FunctionRepository getFunctionRepository() {
    return _functionRepository;
  }

  @Override
  public void run() {
    s_logger.debug("Invoking {} on security {}", getFunctionUniqueIdentifier(), getSecurity());
    FunctionInvoker invoker = getFunctionRepository().getInvoker(getFunctionUniqueIdentifier());
    if(invoker == null) {
      throw new NullPointerException("Unable to locate " + getFunctionUniqueIdentifier() + " in function repository.");
    }
    
    if(getComputationTargetType() == ComputationTarget.MULTIPLE_POSITIONS) {
      s_logger.info("Invoking on multiple positions.");
    }
    
    Collection<ComputedValue<?>> inputs = new HashSet<ComputedValue<?>>();
    for(AnalyticValueDefinition<?> inputDefinition : getResolvedInputs()) {
      ComputedValue<?> input = getComputationCache().getValue(inputDefinition);
      if(input == null) {
        s_logger.info("Not able to execute as missing input {}", inputDefinition);
        throw new MissingInputException(inputDefinition, getFunctionUniqueIdentifier());
      }
      inputs.add(getComputationCache().getValue(inputDefinition));
    }
    FunctionInputs functionInputs = new AnalyticFunctionInputsImpl(inputs);
    
    Collection<ComputedValue<?>> outputs = null;
    if(invoker instanceof PrimitiveFunctionInvoker) {
      outputs = ((PrimitiveFunctionInvoker) invoker).execute(EXECUTION_CONTEXT, functionInputs);
    } else if(invoker instanceof SecurityFunctionInvoker) {
      assert getComputationTargetType() == ComputationTarget.SECURITY;
      outputs = ((SecurityFunctionInvoker) invoker).execute(EXECUTION_CONTEXT, functionInputs, getSecurity());
    } else if(invoker instanceof PositionFunctionInvoker) {
      assert getComputationTargetType() == ComputationTarget.POSITION;
      outputs = ((PositionFunctionInvoker) invoker).execute(EXECUTION_CONTEXT, functionInputs, getPosition());
    } else if(invoker instanceof AggregatePositionFunctionInvoker) {
      assert getComputationTargetType() == ComputationTarget.MULTIPLE_POSITIONS;
      outputs = ((AggregatePositionFunctionInvoker) invoker).execute(EXECUTION_CONTEXT, functionInputs, getPositions());
    } else {
      throw new UnsupportedOperationException("Only primitive and security invokers supported now.");
    }
    for(ComputedValue<?> outputValue : outputs) {
      getComputationCache().putValue(outputValue);
    }
  }

}
