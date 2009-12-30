/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Set;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

// REVIEW kirk 2009-12-29 -- This class will eventually be renamed to
// PrimitiveFunctionDefinition once the full refactor of the engine is complete.

/**
 * 
 *
 * @author kirk
 */
public interface NewPrimitiveFunctionDefinition extends FunctionDefinition {
  
  boolean canApplyTo(Object primitiveKey);
  
  Set<ValueRequirement> getRequirements(Object primitiveKey);
  
  Set<ValueSpecification> getResults(Object primitiveKey);

}
