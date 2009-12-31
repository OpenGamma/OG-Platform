/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.ArgumentChecker;

// REVIEW kirk 2009-12-30 -- Should have indexing possibilities.

/**
 * 
 *
 * @author kirk
 */
public class FunctionInputsImpl implements FunctionInputs, Serializable {
  private final Set<ComputedValue> _values;
  
  public FunctionInputsImpl() {
    _values = new HashSet<ComputedValue>();
  }
  
  public FunctionInputsImpl(Collection<? extends ComputedValue> values) {
    _values = new HashSet<ComputedValue>(values);
  }
  
  public void addValue(ComputedValue value) {
    ArgumentChecker.checkNotNull(value, "Computed Value");
    _values.add(value);
  }

  @Override
  public Collection<ComputedValue> getAllValues() {
    return Collections.unmodifiableSet(_values);
  }

  @Override
  public Object getValue(ValueRequirement requirement) {
    for(ComputedValue value : _values) {
      if(ObjectUtils.equals(requirement, value.getSpecification().getRequirementSpecification())) {
        return value.getValue();
      }
    }
    return null;
  }

  @Override
  public Object getValue(String requirementName) {
    for(ComputedValue value : _values) {
      if(ObjectUtils.equals(requirementName, value.getSpecification().getRequirementSpecification().getValueName())) {
        return value.getValue();
      }
    }
    return null;
  }

}
