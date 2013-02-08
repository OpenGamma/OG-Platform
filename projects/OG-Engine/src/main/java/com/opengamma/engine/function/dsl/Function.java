/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl;


import com.opengamma.engine.target.ComputationTargetType;

public class Function {

  public static FunctionSignature function(String name, ComputationTargetType computationTargetType) {
    return new SimpleFunctionSignature(name, computationTargetType);
  }

  public static FunctionOutput output(String name) {
    return new FunctionOutput(name);
  }

  public static FunctionInput input(String name) {
    return new FunctionInput(name);
  }
  
}
