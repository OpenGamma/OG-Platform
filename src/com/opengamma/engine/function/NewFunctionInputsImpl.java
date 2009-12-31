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

import com.opengamma.engine.value.NewComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.ArgumentChecker;

// REVIEW kirk 2009-12-30 -- Should have indexing possibilities.

/**
 * 
 *
 * @author kirk
 */
public class NewFunctionInputsImpl implements NewFunctionInputs, Serializable {
  private final Set<NewComputedValue> _values;
  
  public NewFunctionInputsImpl() {
    _values = new HashSet<NewComputedValue>();
  }
  
  public NewFunctionInputsImpl(Collection<? extends NewComputedValue> values) {
    _values = new HashSet<NewComputedValue>(values);
  }
  
  public void addValue(NewComputedValue value) {
    ArgumentChecker.checkNotNull(value, "Computed Value");
    _values.add(value);
  }

  @Override
  public Collection<NewComputedValue> getAllValues() {
    return Collections.unmodifiableSet(_values);
  }

  @Override
  public Object getValue(ValueRequirement requirement) {
    for(NewComputedValue value : _values) {
      if(ObjectUtils.equals(requirement, value.getSpecification().getRequirementSpecification())) {
        return value.getValue();
      }
    }
    return null;
  }

  @Override
  public Object getValue(String requirementName) {
    for(NewComputedValue value : _values) {
      if(ObjectUtils.equals(requirementName, value.getSpecification().getRequirementSpecification().getValueName())) {
        return value.getValue();
      }
    }
    return null;
  }

}
