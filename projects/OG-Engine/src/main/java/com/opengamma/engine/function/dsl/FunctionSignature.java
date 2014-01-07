/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl;

import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.lambdava.streams.Functional;

/**
 * DSL function signature.
 */
public interface FunctionSignature {

  FunctionSignature outputs(FunctionOutput... outputs);

  FunctionSignature inputs(FunctionInput... inputs);

  Functional<FunctionOutput> getOutputs();

  Functional<FunctionInput> getInputs();

  String getName();

  ComputationTargetType getComputationTargetType();

  FunctionSignature addInput(FunctionInput input);

  FunctionSignature addOutput(FunctionOutput output);

  Class<?> getComputationTargetClass();

  FunctionSignature targetClass(Class<?> clazz);

}
