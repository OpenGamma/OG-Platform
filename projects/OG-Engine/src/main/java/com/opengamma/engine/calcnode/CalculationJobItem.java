/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2LongMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.cache.IdentifierEncodedValueSpecifications;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLog;
import com.opengamma.engine.view.ExecutionLogMode;

/**
 * 
 */
public final class CalculationJobItem implements IdentifierEncodedValueSpecifications {

  private static final long[] EMPTY_LONG = new long[0];
  private static final ValueSpecification[] EMPTY_VALUESPEC = new ValueSpecification[0];

  // should these two be combined to ParameterizedFunction ID?
  private final String _functionUniqueIdentifier;
  private final FunctionParameters _functionParameters;

  private final ComputationTargetSpecification _computationTargetSpecification;
  private ValueSpecification[] _inputSpecifications;
  private long[] _inputIdentifiers;
  private ValueSpecification[] _outputSpecifications;
  private long[] _outputIdentifiers;

  private final ExecutionLogMode _logMode;

  public CalculationJobItem(String functionUniqueIdentifier, FunctionParameters functionParameters,
      ComputationTargetSpecification computationTargetSpecification, Collection<ValueSpecification> inputs,
      Collection<ValueSpecification> outputs, ExecutionLogMode logMode) {
    _functionUniqueIdentifier = functionUniqueIdentifier;
    _functionParameters = functionParameters;
    _computationTargetSpecification = computationTargetSpecification;
    _inputSpecifications = inputs.toArray(new ValueSpecification[inputs.size()]);
    _outputSpecifications = outputs.toArray(new ValueSpecification[outputs.size()]);
    _logMode = logMode;
  }

  /**
   * Constructs a job item corresponding to a node in a dependency graph.
   * 
   * @param functionUniqueIdentifier the function identifier, not null
   * @param functionParameters the function parameters, not null
   * @param computationTargetSpecification the function's target, never null
   * @param inputs the mapped identifiers for the function's inputs, never null
   * @param outputs the mapped identifiers for the function's output, never null
   * @param logMode the log capturing mode, not null
   */
  public CalculationJobItem(String functionUniqueIdentifier, FunctionParameters functionParameters,
      ComputationTargetSpecification computationTargetSpecification, ValueSpecification[] inputs, ValueSpecification[] outputs,
      ExecutionLogMode logMode) {
    _functionUniqueIdentifier = functionUniqueIdentifier;
    _functionParameters = functionParameters;
    _computationTargetSpecification = computationTargetSpecification;
    _inputSpecifications = inputs;
    _outputSpecifications = outputs;
    _logMode = logMode;
  }

  /**
   * Constructs a job item based on the data used in the network message.
   * 
   * @param functionUniqueIdentifier the function identifier to execute, never null
   * @param functionParameters the function's execution parameters, never null
   * @param computationTargetSpecification the function's target, never null
   * @param inputs the mapped identifiers for the function's inputs, never null
   * @param outputs the mapped identifiers for the function's output, never null
   * @param logMode the log capturing mode, not null
   */
  public CalculationJobItem(String functionUniqueIdentifier, FunctionParameters functionParameters,
      ComputationTargetSpecification computationTargetSpecification, long[] inputs, long[] outputs,
      ExecutionLogMode logMode) {
    _functionUniqueIdentifier = functionUniqueIdentifier;
    _functionParameters = functionParameters;
    _computationTargetSpecification = computationTargetSpecification;
    _inputIdentifiers = inputs;
    _outputIdentifiers = outputs;
    _logMode = logMode;
  }

  //-------------------------------------------------------------------------
  /**
   * @return the functionUniqueIdentifier
   */
  public String getFunctionUniqueIdentifier() {
    return _functionUniqueIdentifier;
  }

  public FunctionParameters getFunctionParameters() {
    return _functionParameters;
  }

  /**
   * Returns the identifiers of the function inputs. The identifier will only be populated after deserialization from a Fudge message or after {@link #convertIdentifiers} has been called.
   * 
   * @return the identifiers or null if they have not been converted
   */
  public long[] getInputIdentifiers() {
    return _inputIdentifiers;
  }

  /**
   * Returns the function input specifications. If the item has been deserialized the specifications will only be populated after {@link #resolveIdentifiers} has been called
   * 
   * @return the input specifications or null if they have not been resolved
   */
  public ValueSpecification[] getInputs() {
    return _inputSpecifications;
  }

  /**
   * Returns the identifiers of the function outputs. The identifiers will only be populated after deserialization from a Fudge message or after {@link #convertIdentifiers} has been called.
   * 
   * @return the identifiers or null if they have not been converted
   */
  public long[] getOutputIdentifiers() {
    return _outputIdentifiers;
  }

  /**
   * Returns the function output specifications. If the item has been deserialized the specifications will only be populated after {@link #resolveIdentifiers} has been called.
   * 
   * @return the output specifications or null if they have not been converted
   */
  public ValueSpecification[] getOutputs() {
    return _outputSpecifications;
  }

  /**
   * @return the computationTargetSpecification
   */
  public ComputationTargetSpecification getComputationTargetSpecification() {
    return _computationTargetSpecification;
  }

  /**
   * Gets the execution log mode, controlling the level of detail in the {@link ExecutionLog} present in the results.
   * 
   * @return the execution log mode, not null
   */
  public ExecutionLogMode getLogMode() {
    return _logMode;
  }

  //-------------------------------------------------------------------------
  @Override
  public void convertIdentifiers(final Long2ObjectMap<ValueSpecification> identifiers) {
    if (_inputSpecifications == null) {
      if (_inputIdentifiers.length > 0) {
        _inputSpecifications = new ValueSpecification[_inputIdentifiers.length];
        for (int i = 0; i < _inputIdentifiers.length; i++) {
          _inputSpecifications[i] = identifiers.get(_inputIdentifiers[i]);
        }
      } else {
        _inputSpecifications = EMPTY_VALUESPEC;
      }
    }
    if (_outputSpecifications == null) {
      if (_outputIdentifiers.length > 0) {
        _outputSpecifications = new ValueSpecification[_outputIdentifiers.length];
        for (int i = 0; i < _outputIdentifiers.length; i++) {
          _outputSpecifications[i] = identifiers.get(_outputIdentifiers[i]);
        }
      } else {
        _outputSpecifications = EMPTY_VALUESPEC;
      }
    }
  }

  @Override
  public void collectIdentifiers(final LongSet identifiers) {
    for (long identifier : _inputIdentifiers) {
      identifiers.add(identifier);
    }
    for (long identifier : _outputIdentifiers) {
      identifiers.add(identifier);
    }
  }

  @Override
  public void convertValueSpecifications(final Object2LongMap<ValueSpecification> valueSpecifications) {
    if (_inputIdentifiers == null) {
      if (_inputSpecifications.length > 0) {
        _inputIdentifiers = new long[_inputSpecifications.length];
        for (int i = 0; i < _inputSpecifications.length; i++) {
          _inputIdentifiers[i] = valueSpecifications.getLong(_inputSpecifications[i]);
        }
      } else {
        _inputIdentifiers = EMPTY_LONG;
      }
    }
    if (_outputIdentifiers == null) {
      if (_outputSpecifications.length > 0) {
        _outputIdentifiers = new long[_outputSpecifications.length];
        for (int i = 0; i < _outputSpecifications.length; i++) {
          _outputIdentifiers[i] = valueSpecifications.getLong(_outputSpecifications[i]);
        }
      } else {
        _outputIdentifiers = EMPTY_LONG;
      }
    }
  }

  @Override
  public void collectValueSpecifications(final Set<ValueSpecification> valueSpecifications) {
    for (ValueSpecification input : _inputSpecifications) {
      valueSpecifications.add(input);
    }
    for (ValueSpecification output : _outputSpecifications) {
      valueSpecifications.add(output);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return new ToStringBuilder(this).append("Function unique ID", getFunctionUniqueIdentifier()).append("Computation target", getComputationTargetSpecification()).toString();
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object o) {
    if (!(o instanceof CalculationJobItem)) {
      return false;
    }
    final CalculationJobItem other = (CalculationJobItem) o;
    return _functionUniqueIdentifier.equals(other._functionUniqueIdentifier) && _computationTargetSpecification.equals(other._computationTargetSpecification) &&
        Arrays.deepEquals(_inputSpecifications, other._inputSpecifications) && Arrays.deepEquals(_outputSpecifications, other._outputSpecifications);
  }

  @Override
  public int hashCode() {
    final int multiplier = 17;
    int hc = 1;
    hc += _functionUniqueIdentifier.hashCode() * multiplier;
    hc *= multiplier;
    hc += _computationTargetSpecification.hashCode();
    hc *= multiplier;
    if (_inputSpecifications != null) {
      hc += Arrays.hashCode(_inputSpecifications);
    }
    hc *= multiplier;
    if (_outputSpecifications != null) {
      hc += Arrays.hashCode(_outputSpecifications);
    }
    hc *= multiplier;
    return hc;
  }

}
