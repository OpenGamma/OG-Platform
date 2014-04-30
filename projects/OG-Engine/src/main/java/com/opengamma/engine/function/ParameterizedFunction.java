/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import com.opengamma.engine.depgraph.DependencyNodeFunction;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * A function configured to be invoked with certain parameters.
 * <p>
 * For example, suppose you have a function, PriceCDOMonteCarlo, which takes one parameter, IterationCount. You could then have two {@code ParameterizedFunctions}, one where IterationCount = 20000 and
 * another where IterationCount = 50000.
 */
@PublicAPI
public class ParameterizedFunction implements DependencyNodeFunction {

  private String _uniqueId;
  private final CompiledFunctionDefinition _function;
  private final FunctionParameters _parameters;

  /**
   * Creates a function/parameter pair.
   * 
   * @param function the function definition, not null
   * @param parameters the function parameters, not null
   */
  public ParameterizedFunction(final CompiledFunctionDefinition function, final FunctionParameters parameters) {
    ArgumentChecker.notNull(function, "function");
    ArgumentChecker.notNull(parameters, "parameters");

    _function = function;
    _parameters = parameters;
  }

  /**
   * Returns the unique identifier of the parameterized function, if set.
   * 
   * @return the unique identifier, null if none is set
   */
  public String getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the unique identifier of the parameterized function.
   * 
   * @param uniqueId the unique identifier
   */
  public void setUniqueId(final String uniqueId) {
    _uniqueId = uniqueId;
  }

  /**
   * Returns the function definition.
   * 
   * @return the function definition
   */
  public CompiledFunctionDefinition getFunction() {
    return _function;
  }

  /**
   * Returns the function identifier.
   * 
   * @return the function identifier
   */
  @Override
  public String getFunctionId() {
    return getFunction().getFunctionDefinition().getUniqueId();
  }

  /**
   * Returns the function parameters.
   * 
   * @return the function parameters
   */
  @Override
  public FunctionParameters getParameters() {
    return _parameters;
  }

  @Override
  public String toString() {
    return "ParameterizedFunction[" + getFunctionId() + ", " + getParameters() + "]";
  }

  @Override
  public int hashCode() {
    if (_uniqueId != null) {
      return _uniqueId.hashCode();
    }
    return _function.hashCode() ^ _parameters.hashCode();
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ParameterizedFunction)) {
      return false;
    }
    final ParameterizedFunction other = (ParameterizedFunction) o;
    if (_uniqueId != null) {
      return _uniqueId.equals(other._uniqueId);
    }
    if (_function != other._function) {
      FunctionDefinition myFunction = _function.getFunctionDefinition();
      FunctionDefinition otherFunction = other._function.getFunctionDefinition();
      if (myFunction != otherFunction) {
        if (!myFunction.getUniqueId().equals(otherFunction.getUniqueId())) {
          return false;
        }
      }
    }
    return _parameters.equals(other._parameters);
  }
}
