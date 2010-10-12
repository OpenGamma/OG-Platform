/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * A function configured to be invoked with certain parameters.
 * <p>
 * For example, suppose you have a function, PriceCDOMonteCarlo, which
 * takes one parameter, IterationCount.
 * You could then have two {@code ParameterizedFunctions}, one where
 * IterationCount = 20000 and another where IterationCount = 50000. 
 */
@PublicAPI
public class ParameterizedFunction {
  
  private String _uniqueId;
  private final FunctionDefinition _function;
  private final FunctionParameters _parameters;
  
  public ParameterizedFunction(FunctionDefinition function,
      FunctionParameters parameters) {
    ArgumentChecker.notNull(function, "function");
    ArgumentChecker.notNull(parameters, "parameters");
    
    _function = function;
    _parameters = parameters;
  }

  public String getUniqueId() {
    return _uniqueId;
  }
  
  public void setUniqueId(String uniqueId) {
    _uniqueId = uniqueId;
  }

  public FunctionDefinition getFunction() {
    return _function;
  }

  public FunctionParameters getParameters() {
    return _parameters;
  }
  
}
