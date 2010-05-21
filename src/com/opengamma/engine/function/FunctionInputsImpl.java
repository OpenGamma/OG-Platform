/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
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

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class FunctionInputsImpl implements FunctionInputs, Serializable {
  private final Set<ComputedValue> _values = new HashSet<ComputedValue>();
  private final Map<String, Object> _valuesByRequirementName = new HashMap<String, Object>();
  private final Map<ValueRequirement, Object> _valuesByRequirement = new HashMap<ValueRequirement, Object>();
  
  public FunctionInputsImpl() {
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
    _valuesByRequirementName.put(value.getSpecification().getRequirementSpecification().getValueName(), value.getValue());
    _valuesByRequirement.put(value.getSpecification().getRequirementSpecification(), value.getValue());
  }

  @Override
  public Collection<ComputedValue> getAllValues() {
    return Collections.unmodifiableSet(_values);
  }

  @Override
  public Object getValue(ValueRequirement requirement) {
    return _valuesByRequirement.get(requirement);
  }

  @Override
  public Object getValue(String requirementName) {
    return _valuesByRequirementName.get(requirementName);
  }

}
