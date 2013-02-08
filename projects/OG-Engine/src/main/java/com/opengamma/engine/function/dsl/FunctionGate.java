/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.dsl.properties.RecordingValueProperties;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.id.UniqueId;

public class FunctionGate<T extends FunctionGate> {
  public static final String NAME = "NAME";
  public static final String TYPE = "TYPE";
  public static final String PROPERTIES = "PROPERTIES";
  private String _name;
  private ComputationTargetSpecification _cts;
  private ValueProperties _Dsl_valueProperties;
  private RecordingValueProperties _recordingValueProperties;
  private TargetSpecificationReference _targetSpecificationReference;

  public FunctionGate(String name) {
    _name = name;
  }

  public T properties(ValueProperties valueProperties) {
    _Dsl_valueProperties = valueProperties;
    return (T) this;
  }

  public T properties(ValueProperties.Builder builder) {
    _Dsl_valueProperties = builder.get();
    return (T) this;
  }

  public ValueProperties getValueProperties() {
    return _Dsl_valueProperties;
  }

  public RecordingValueProperties getRecordingValueProperties() {
    return _recordingValueProperties;
  }

  public T properties(RecordingValueProperties recordingValueProperties) {
    _recordingValueProperties = recordingValueProperties;
    return (T) this;
  }

  public String getName() {
    return _name;
  }

  public T targetSpec(ComputationTargetType computationTargetType, UniqueId uid) {
    _cts = new ComputationTargetSpecification(computationTargetType, uid);
    return (T) this;
  }

  public T targetSpec(ComputationTargetSpecification computationTargetSpecification) {
    _cts = computationTargetSpecification;
    return (T) this;
  }

  public ComputationTargetSpecification getComputationTargetSpecification() {
    return _cts;
  }

  public T targetSpec(TargetSpecificationReference targetSpecificationReference) {
    _targetSpecificationReference = targetSpecificationReference;
    return (T) this;
  }

  public TargetSpecificationReference getTargetSpecificationReference() {
    return _targetSpecificationReference;
  }

}
