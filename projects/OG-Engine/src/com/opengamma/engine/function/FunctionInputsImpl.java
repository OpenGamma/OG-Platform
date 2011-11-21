/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * An implementation of {@link FunctionInputs} that stores all inputs in internal maps.
 *
 */
public class FunctionInputsImpl implements FunctionInputs, Serializable {
  
  private static final long serialVersionUID = 1L;
  
  private final Set<ComputedValue> _values = new HashSet<ComputedValue>();
  private final Map<String, Object> _valuesByRequirementName = new HashMap<String, Object>();
  private final Map<Pair<String, ComputationTargetSpecification>, ComputedValue[]> _valuesByRequirement = new HashMap<Pair<String, ComputationTargetSpecification>, ComputedValue[]>();

  public FunctionInputsImpl() {
  }

  public FunctionInputsImpl(ComputedValue value) {
    this(Collections.singleton(value));
  }

  public FunctionInputsImpl(Collection<? extends ComputedValue> values) {
    for (ComputedValue value : values) {
      addValue(value);
    }
  }

  public void addValue(ComputedValue value) {
    ArgumentChecker.notNull(value, "Computed Value");
    if (value.getValue() instanceof ComputedValue) {
      throw new IllegalArgumentException("Double-nested value");
    }
    _values.add(value);
    _valuesByRequirementName.put(value.getSpecification().getValueName(), value.getValue());
    final Pair<String, ComputationTargetSpecification> key = Pair.of(value.getSpecification().getValueName(), value.getSpecification().getTargetSpecification());
    final ComputedValue[] prev = _valuesByRequirement.get(key);
    if (prev == null) {
      _valuesByRequirement.put(key, new ComputedValue[] {value});
    } else {
      final ComputedValue[] values = new ComputedValue[prev.length + 1];
      System.arraycopy(prev, 0, values, 0, prev.length);
      values[prev.length] = value;
      _valuesByRequirement.put(key, values);
    }
  }

  @Override
  public Collection<ComputedValue> getAllValues() {
    return Collections.unmodifiableSet(_values);
  }

  @Override
  public Object getValue(ValueRequirement requirement) {
    ComputedValue cv = getComputedValue(requirement);
    if (cv != null) {
      return cv.getValue();
    }
    return null;
  }

  @Override
  public ComputedValue getComputedValue(ValueRequirement requirement) {
    final Pair<String, ComputationTargetSpecification> key = Pair.of(requirement.getValueName(),
        requirement.getTargetSpecification());
    final ComputedValue[] values = _valuesByRequirement.get(key);
    if (values != null) {
      for (ComputedValue value : values) {
        // Shortcut to check the properties as we already know the name and target match  
        if (requirement.getConstraints().isSatisfiedBy(value.getSpecification().getProperties())) {
          return value;
        }
      }
    }
    return null;
  }

  @Override
  public Object getValue(String requirementName) {
    return _valuesByRequirementName.get(requirementName);
  }

}
