/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collection;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;

/**
 * 
 *
 * @author kirk
 */
public interface FunctionInputs {
  Collection<ComputedValue> getAllValues();
  
  Object getValue(ValueRequirement requirement);
  
  Object getValue(String requirementName);

}
