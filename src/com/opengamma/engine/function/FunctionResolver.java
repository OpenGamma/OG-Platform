/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.Pair;

/**
 * The function resolver is responsible for matching the requirements of a particular computation target and value requirement
 * to a given function.  It is separated from the FunctionRepository so different implementations can be plugged in and used to
 * match functions given different criteria e.g. Optimized for speed.
 * @author jim
 */
public interface FunctionResolver {
  public Pair<FunctionDefinition, ValueSpecification> resolveFunction(ComputationTarget target, ValueRequirement requirement);
}
