/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collection;

import com.opengamma.engine.value.NewComputedValue;
import com.opengamma.engine.value.ValueRequirement;

/**
 * 
 *
 * @author kirk
 */
public interface NewFunctionInputs {
  Collection<NewComputedValue> getAllValues();
  
  Object getValue(ValueRequirement requirement);
  
  Object getValue(String requirementName);

}
