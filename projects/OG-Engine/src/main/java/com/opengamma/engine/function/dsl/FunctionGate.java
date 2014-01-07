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

/**
 * A function gate.
 * 
 * @param <T>  the sub-type
 */
public class FunctionGate<T extends FunctionGate<T>> {

  /**
   * The name.
   */
  public static final String NAME = "NAME";
  /**
   * The type.
   */
  public static final String TYPE = "TYPE";
  /**
   * The properties.
   */
  public static final String PROPERTIES = "PROPERTIES";

  private String _name;
  private ComputationTargetSpecification _cts;
  private ValueProperties _dslValueProperties;
  private RecordingValueProperties _recordingValueProperties;
  private TargetSpecificationReference _targetSpecificationReference;

  public FunctionGate(String name) {
    _name = name;
  }

  @SuppressWarnings("unchecked")
  private T subType() {
    return (T) this;
  }

  //-------------------------------------------------------------------------
  public T properties(ValueProperties valueProperties) {
    _dslValueProperties = valueProperties;
    return subType();
  }

  public T properties(ValueProperties.Builder builder) {
    _dslValueProperties = builder.get();
    return subType();
  }

  public ValueProperties getValueProperties() {
    return _dslValueProperties;
  }

  public RecordingValueProperties getRecordingValueProperties() {
    return _recordingValueProperties;
  }

  public T properties(RecordingValueProperties recordingValueProperties) {
    _recordingValueProperties = recordingValueProperties;
    return subType();
  }

  public String getName() {
    return _name;
  }

  public T targetSpec(ComputationTargetType computationTargetType, UniqueId uid) {
    _cts = new ComputationTargetSpecification(computationTargetType, uid);
    return subType();
  }

  public T targetSpec(ComputationTargetSpecification computationTargetSpecification) {
    _cts = computationTargetSpecification;
    return subType();
  }

  public ComputationTargetSpecification getComputationTargetSpecification() {
    return _cts;
  }

  public T targetSpec(TargetSpecificationReference targetSpecificationReference) {
    _targetSpecificationReference = targetSpecificationReference;
    return subType();
  }

  public TargetSpecificationReference getTargetSpecificationReference() {
    return _targetSpecificationReference;
  }

}
