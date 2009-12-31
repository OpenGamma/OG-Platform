/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 *
 * @author kirk
 */
public interface NewFunctionDefinition extends FunctionDefinition {

  boolean canApplyTo(ComputationTarget target);
  
  Set<ValueRequirement> getRequirements(ComputationTarget target);
  
  Set<ValueSpecification> getResults(ComputationTarget target, Set<ValueRequirement> requirements);
}
