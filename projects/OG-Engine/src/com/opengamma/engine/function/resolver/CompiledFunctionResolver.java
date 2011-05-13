/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import java.util.Collection;
import java.util.Iterator;

import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.UnsatisfiableDependencyGraphException;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.tuple.Pair;

/**
 * Returned by a {@link FunctionResolver} to do the actual resolution for a specific timestamp.
 */
@PublicAPI
public interface CompiledFunctionResolver {
  
  /**
   * Returns one or more functions capable of satisfying the requirement. If multiple functions can satisfy, they
   * should be returned in descending priority.
   * 
   * @param requirement Output requirement to satisfy
   * @param atNode The node in a dependency graph the function would be assigned to
   * @return the function(s) found
   * @throws UnsatisfiableDependencyGraphException if there is a problem
   */
  Iterator<Pair<ParameterizedFunction, ValueSpecification>> resolveFunction(ValueRequirement requirement, DependencyNode atNode);

  /**
   * Returns a full set of resolution rules backing the resolver.
   * 
   * @return the full set of resolution rules, not {@code null}
   */
  Collection<ResolutionRule> getAllResolutionRules();

}
