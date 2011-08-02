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
 * Resolver returned by {@link FunctionResolver} to do the actual resolution for a specific timestamp.
 */
@PublicAPI
public interface CompiledFunctionResolver {

  /**
   * Resolves the requirement for a node to one or more functions.
   * <p>
   * The resolution finds functions that are capable of satisfying the requirement.
   * If multiple functions can satisfy, they should be returned from highest priority
   * to lowest priority.
   * 
   * @param requirement   the output requirement to satisfy, not null
   * @param atNode  the node in a dependency graph the function would be assigned to, not null
   * @return one or more matching functions, from highest to lowest priority, not null
   * @throws UnsatisfiableDependencyGraphException if there is a problem
   */
  Iterator<Pair<ParameterizedFunction, ValueSpecification>> resolveFunction(ValueRequirement requirement, DependencyNode atNode);

  /**
   * Gets the full set of resolution rules backing the resolver.
   * 
   * @return the full set of resolution rules, not null
   */
  Collection<ResolutionRule> getAllResolutionRules();

}
