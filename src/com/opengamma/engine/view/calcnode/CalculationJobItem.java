/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.IdentifierMap;

/**
 * 
 */
public final class CalculationJobItem {

  private static final String FUNCTION_UNIQUE_ID_FIELD_NAME = "functionUniqueIdentifier";
  private static final String INPUT_FIELD_NAME = "valueInput";
  private static final String DESIRED_VALUE_FIELD_NAME = "desiredValue";

  private final String _functionUniqueIdentifier;
  private final ComputationTargetSpecification _computationTargetSpecification;
  private final Set<ValueSpecification> _inputs = new HashSet<ValueSpecification>();
  private Collection<Long> _inputIdentifiers;
  private final Set<ValueRequirement> _desiredValues = new HashSet<ValueRequirement>();

  public CalculationJobItem(String functionUniqueIdentifier, ComputationTargetSpecification computationTargetSpecification, Collection<ValueSpecification> inputs,
      Collection<ValueRequirement> desiredValues) {
    _functionUniqueIdentifier = functionUniqueIdentifier;
    _computationTargetSpecification = computationTargetSpecification;
    _inputs.addAll(inputs);
    _desiredValues.addAll(desiredValues);
  }

  private CalculationJobItem(String functionUniqueIdentifier, ComputationTargetSpecification computationTargetSpecification, Collection<ValueSpecification> inputs, Collection<Long> inputIdentifiers,
      Collection<ValueRequirement> desiredValues) {
    _functionUniqueIdentifier = functionUniqueIdentifier;
    _computationTargetSpecification = computationTargetSpecification;
    if (inputs != null) {
      _inputs.addAll(inputs);
    }
    _inputIdentifiers = inputIdentifiers;
    _desiredValues.addAll(desiredValues);
  }

  /**
   * @return the functionUniqueIdentifier
   */
  public String getFunctionUniqueIdentifier() {
    return _functionUniqueIdentifier;
  }

  /**
   * @return the inputs
   */
  public Set<ValueSpecification> getInputs() {
    return Collections.unmodifiableSet(_inputs);
  }

  /**
   * Numeric identifiers may have been passed when this was encoded as a Fudge message. This will resolve
   * them to full {@link ValueSpecification} objects.
   * 
   * @param identifierMap Identifier map to resolve the inputs with
   */
  public void resolveInputs(final IdentifierMap identifierMap) {
    if (_inputs.isEmpty()) {
      if (_inputIdentifiers.size() == 1) {
        _inputs.add(identifierMap.getValueSpecification(_inputIdentifiers.iterator().next()));
      } else {
        _inputs.addAll(identifierMap.getValueSpecifications(_inputIdentifiers).values());
      }
    }
  }

  /**
   * Convert full {@link ValueSpecification} objects to numeric identifiers for more efficient Fudge
   * encoding.
   * 
   * @param identifierMap Identifier map to convert the inputs with
   */
  public void convertInputs(final IdentifierMap identifierMap) {
    if (_inputIdentifiers == null) {
      if (_inputs.size() == 1) {
        _inputIdentifiers = Collections.singleton(identifierMap.getIdentifier(_inputs.iterator().next()));
      } else {
        _inputIdentifiers = identifierMap.getIdentifiers(_inputs).values();
      }
    }
  }

  /**
   * @return the computationTargetSpecification
   */
  public ComputationTargetSpecification getComputationTargetSpecification() {
    return _computationTargetSpecification;
  }

  /**
   * @return the desiredValues
   */
  public Set<ValueRequirement> getDesiredValues() {
    return Collections.unmodifiableSet(_desiredValues);
  }

  public Set<ValueSpecification> getOutputs() {
    Set<ValueSpecification> outputs = new HashSet<ValueSpecification>();
    for (ValueRequirement requirement : getDesiredValues()) {
      outputs.add(new ValueSpecification(requirement, getFunctionUniqueIdentifier()));
    }
    return outputs;
  }

  public FudgeFieldContainer toFudgeMsg(FudgeSerializationContext fudgeContext) {
    MutableFudgeFieldContainer msg = fudgeContext.newMessage();

    getComputationTargetSpecification().toFudgeMsg(fudgeContext, msg);
    msg.add(FUNCTION_UNIQUE_ID_FIELD_NAME, getFunctionUniqueIdentifier());

    if (_inputIdentifiers != null) {
      for (Long identifier : _inputIdentifiers) {
        msg.add(INPUT_FIELD_NAME, identifier);
      }
    } else {
      for (ValueSpecification inputSpecification : _inputs) {
        msg.add(INPUT_FIELD_NAME, inputSpecification.toFudgeMsg(fudgeContext));
      }
    }

    for (ValueRequirement desiredValue : getDesiredValues()) {
      MutableFudgeFieldContainer valueMsg = fudgeContext.newMessage();
      desiredValue.toFudgeMsg(fudgeContext, valueMsg);
      msg.add(DESIRED_VALUE_FIELD_NAME, valueMsg);
    }

    return msg;
  }

  public static CalculationJobItem fromFudgeMsg(FudgeDeserializationContext fudgeContext, FudgeFieldContainer msg) {
    String functionUniqueId = msg.getString(FUNCTION_UNIQUE_ID_FIELD_NAME);

    ComputationTargetSpecification computationTargetSpecification = ComputationTargetSpecification.fromFudgeMsg(msg);

    final List<FudgeField> inputFields = msg.getAllByName(INPUT_FIELD_NAME);
    List<ValueSpecification> inputSpecifications = null;
    List<Long> inputIdentifiers = null;
    for (FudgeField field : inputFields) {
      final Object value = field.getValue();
      if (value instanceof Number) {
        if (inputIdentifiers == null) {
          inputIdentifiers = new ArrayList<Long>(inputFields.size());
        }
        inputIdentifiers.add(((Number) value).longValue());
      } else {
        if (inputSpecifications == null) {
          inputSpecifications = new ArrayList<ValueSpecification>(inputFields.size());
        }
        inputSpecifications.add(ValueSpecification.fromFudgeMsg(fudgeContext, (FudgeFieldContainer) field.getValue()));
      }
    }

    List<ValueRequirement> desiredValues = new ArrayList<ValueRequirement>();
    for (FudgeField field : msg.getAllByName(DESIRED_VALUE_FIELD_NAME)) {
      FudgeFieldContainer valueMsg = (FudgeFieldContainer) field.getValue();
      ValueRequirement desiredValue = ValueRequirement.fromFudgeMsg(valueMsg);
      desiredValues.add(desiredValue);
    }

    return new CalculationJobItem(functionUniqueId, computationTargetSpecification, inputSpecifications, inputIdentifiers, desiredValues);
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
    return _functionUniqueIdentifier.equals(other._functionUniqueIdentifier) && _computationTargetSpecification.equals(other._computationTargetSpecification) && _inputs.equals(other._inputs)
        && _desiredValues.equals(other._desiredValues);
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
    hc += _desiredValues.hashCode();
    hc *= multiplier;
    return hc;
  }

}
