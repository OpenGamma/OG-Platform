/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.tuple.Pair;

/**
 * Returned by a {@link FunctionResolver} to do the actual resolution.
 */
@PublicAPI
public interface CompiledFunctionResolver {
  
  Pair<ParameterizedFunction, ValueSpecification> resolveFunction(ValueRequirement requirement, DependencyNode atNode);
  
}
