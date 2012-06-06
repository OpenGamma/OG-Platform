/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.IdentifierMap;

/**
 * 
 */
public final class CalculationJobItem {

  private static final long[] EMPTY = new long[0];

  // should these two be combined to ParameterizedFunction ID?
  private final String _functionUniqueIdentifier;
  private final FunctionParameters _functionParameters;

  private final ComputationTargetSpecification _computationTargetSpecification;
  private final Set<ValueSpecification> _inputs = new HashSet<ValueSpecification>();
  private long[] _inputIdentifiers;
  private final Set<ValueSpecification> _outputs = new HashSet<ValueSpecification>();
  private long[] _outputIdentifiers;

  public CalculationJobItem(String functionUniqueIdentifier, FunctionParameters functionParameters, ComputationTargetSpecification computationTargetSpecification,
      Collection<ValueSpecification> inputs, Collection<ValueSpecification> outputs) {
    _functionUniqueIdentifier = functionUniqueIdentifier;
    _functionParameters = functionParameters;
    _computationTargetSpecification = computationTargetSpecification;
    _inputs.addAll(inputs);
    _outputs.addAll(outputs);
  }

  public CalculationJobItem(String functionUniqueIdentifier, FunctionParameters functionParameters, ComputationTargetSpecification computationTargetSpecification, long[] inputs, long[] outputs) {
    _functionUniqueIdentifier = functionUniqueIdentifier;
    _functionParameters = functionParameters;
    _computationTargetSpecification = computationTargetSpecification;
    _inputIdentifiers = inputs;
    _outputIdentifiers = outputs;
  }

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
  public Set<ValueSpecification> getInputs() {
    return Collections.unmodifiableSet(_inputs);
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
   * @return the output specifications
   */
  public Set<ValueSpecification> getOutputs() {
    return Collections.unmodifiableSet(_outputs);
  }

  /**
   * Numeric identifiers may have been passed when this was encoded as a Fudge message. This will resolve them to full {@link ValueSpecification} objects.
   * 
   * @param identifierMap Identifier map to resolve the inputs with
   */
  public void resolveIdentifiers(final IdentifierMap identifierMap) {
    if (_inputs.isEmpty() && (_inputIdentifiers.length > 0)) {
      if (_inputIdentifiers.length == 1) {
        _inputs.add(identifierMap.getValueSpecification(_inputIdentifiers[0]));
      } else {
        final Collection<Long> identifiers = new ArrayList<Long>(_inputIdentifiers.length);
        for (Long identifier : _inputIdentifiers) {
          identifiers.add(identifier);
        }
        _inputs.addAll(identifierMap.getValueSpecifications(identifiers).values());
      }
    }
    if (_outputs.isEmpty() && (_outputIdentifiers.length > 0)) {
      if (_outputIdentifiers.length == 1) {
        _outputs.add(identifierMap.getValueSpecification(_outputIdentifiers[0]));
      } else {
        final Collection<Long> identifiers = new ArrayList<Long>(_outputIdentifiers.length);
        for (Long identifier : _outputIdentifiers) {
          identifiers.add(identifier);
        }
        _outputs.addAll(identifierMap.getValueSpecifications(identifiers).values());
      }
    }
  }

  /**
   * Convert full {@link ValueSpecification} objects to numeric identifiers for more efficient Fudge
   * encoding.
   * 
   * @param identifierMap Identifier map to convert the inputs with
   */
  public void convertIdentifiers(final IdentifierMap identifierMap) {
    if (_inputIdentifiers == null) {
      if (_inputs.isEmpty()) {
        _inputIdentifiers = EMPTY;
      } else if (_inputs.size() == 1) {
        _inputIdentifiers = new long[] {identifierMap.getIdentifier(_inputs.iterator().next())};
      } else {
        final Collection<Long> identifiers = identifierMap.getIdentifiers(_inputs).values();
        _inputIdentifiers = new long[identifiers.size()];
        int i = 0;
        for (Long identifier : identifiers) {
          _inputIdentifiers[i++] = identifier;
        }
      }
    }
    if (_outputIdentifiers == null) {
      if (_outputs.isEmpty()) {
        _outputIdentifiers = EMPTY;
      } else if (_outputs.size() == 1) {
        _outputIdentifiers = new long[] {identifierMap.getIdentifier(_outputs.iterator().next()) };
      } else {
        final Collection<Long> identifiers = identifierMap.getIdentifiers(_outputs).values();
        _outputIdentifiers = new long[identifiers.size()];
        int i = 0;
        for (Long identifier : identifiers) {
          _outputIdentifiers[i++] = identifier;
        }
      }
    }
  }

  /**
   * @return the computationTargetSpecification
   */
  public ComputationTargetSpecification getComputationTargetSpecification() {
    return _computationTargetSpecification;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("Function unique ID", getFunctionUniqueIdentifier()).append("Computation target", getComputationTargetSpecification()).toString();
  }

  @Override
  public boolean equals(final Object o) {
    if (!(o instanceof CalculationJobItem)) {
      return false;
    }
    final CalculationJobItem other = (CalculationJobItem) o;
    return _functionUniqueIdentifier.equals(other._functionUniqueIdentifier) && _computationTargetSpecification.equals(other._computationTargetSpecification) && _inputs.equals(other._inputs) &&
        _outputs.equals(other._outputs);
  }

  @Override
  public int hashCode() {
    final int multiplier = 17;
    int hc = 1;
    hc += _functionUniqueIdentifier.hashCode() * multiplier;
    hc *= multiplier;
    hc += _computationTargetSpecification.hashCode();
    hc *= multiplier;
    hc += _inputs.hashCode();
    hc *= multiplier;
    hc += _outputs.hashCode();
    hc *= multiplier;
    return hc;
  }

}
