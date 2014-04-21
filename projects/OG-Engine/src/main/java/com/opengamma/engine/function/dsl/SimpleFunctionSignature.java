/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl;

import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.lambdava.streams.Functional;
import com.opengamma.lambdava.streams.Stream;

/**
 * A simple function signature.
 */
class SimpleFunctionSignature implements FunctionSignature {

  private String _name;
  private Functional<FunctionOutput> _outputs = Stream.empty();
  private Functional<FunctionInput> _inputs = Stream.empty();
  private ComputationTargetType _computationTargetType;
  private Class<?> _computationTargetClass;

  SimpleFunctionSignature(String name, ComputationTargetType computationTargetType) {
    _name = name;
    _computationTargetType = computationTargetType;
  }

  public String getName() {
    return _name;
  }

  public ComputationTargetType getComputationTargetType() {
    return _computationTargetType;
  }

  @Override
  public Class<?> getComputationTargetClass() {
    return _computationTargetClass;
  }

  @Override
  public FunctionSignature addInput(FunctionInput input) {
    SimpleFunctionSignature signature = new SimpleFunctionSignature(_name, _computationTargetType);
    signature.setInputs(_inputs.cons(input));
    return signature;
  }

  @Override
  public FunctionSignature addOutput(FunctionOutput output) {
    SimpleFunctionSignature signature = new SimpleFunctionSignature(_name, _computationTargetType);
    signature.setOutputs(_outputs.cons(output));
    return signature;
  }

  public FunctionSignature outputs(FunctionOutput... outputs) {
    _outputs = Stream.of(outputs);
    return this;
  }

  public FunctionSignature inputs(FunctionInput... inputs) {
    _inputs = Stream.of(inputs);
    return this;
  }

  @Override
  public FunctionSignature targetClass(Class<?> clazz) {
    _computationTargetClass = clazz;
    return this;
  }

  @Override
  public Functional<FunctionOutput> getOutputs() {
    return _outputs;
  }

  private void setOutputs(Functional<FunctionOutput> outputs) {
    _outputs = outputs;
  }

  @Override
  public Functional<FunctionInput> getInputs() {
    return _inputs;
  }

  private void setInputs(Functional<FunctionInput> inputs) {
    _inputs = inputs;
  }
}
