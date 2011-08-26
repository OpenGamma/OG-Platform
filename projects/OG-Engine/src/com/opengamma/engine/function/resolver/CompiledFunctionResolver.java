/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import java.util.Collection;
import java.util.Iterator;

import com.opengamma.engine.depgraph.DependencyNode;
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

  // PLAT-1049 version
  //  /**
  //   * Resolves the requirement for a node to one or more functions.
  //   * <p>
  //   * The resolution finds functions that are capable of satisfying the requirement.
  //   * If multiple functions can satisfy, they should be returned from highest priority
  //   * to lowest priority.
  //   * 
  //   * @param requirement Output requirement to satisfy
  //   * @param target Target to satisfy the requirement on
  //   * @return the function(s) found
  //   */
  //  Iterator<Pair<ParameterizedFunction, ValueSpecification>> resolveFunction(ValueRequirement requirement, ComputationTarget target);

  /**
   * Resolves the requirement for a node to one or more functions.
   * <p>
   * The resolution finds functions that are capable of satisfying the requirement.
   * If multiple functions can satisfy, they should be returned from highest priority
   * to lowest priority.
   * 
   * @param requirement Output requirement to satisfy
   * @param atNode The node in a dependency graph the function would be assigned to
   * @return the function(s) found
   */
  Iterator<Pair<ParameterizedFunction, ValueSpecification>> resolveFunction(ValueRequirement requirement, DependencyNode atNode);

  /**
   * Gets the full set of resolution rules backing the resolver.
   * 
   * @return the full set of resolution rules, not null
   */
  Collection<ResolutionRule> getAllResolutionRules();

}
