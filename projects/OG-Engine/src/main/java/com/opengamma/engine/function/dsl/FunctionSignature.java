package com.opengamma.engine.function.dsl;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.lambdava.streams.Functional;
import com.opengamma.lambdava.tuple.Pair;

import java.util.Map;


/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p/>
 * Please see distribution for license.
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

  Class getComputationTargetClass();

  FunctionSignature targetClass(Class<?> clazz);
}
