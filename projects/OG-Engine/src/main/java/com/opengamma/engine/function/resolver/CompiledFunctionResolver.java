/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import java.util.Collection;
import java.util.Iterator;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.tuple.Triple;

/**
 * Resolver returned by {@link FunctionResolver} to do the actual resolution for a specific timestamp.
 */
@PublicAPI
public interface CompiledFunctionResolver {

  /**
   * Resolves the requirement for a node to one or more functions.
   * <p>
   * The resolution finds functions that are capable of satisfying the requirement. If multiple functions can satisfy, they should be returned from highest priority to lowest priority.
   * 
   * @param valueName Value requirement name to satisfy
   * @param target Target to satisfy the requirement on
   * @param constraints Constraints that the outputs must satisfy
   * @return the function(s) found, the specification from the output set that matches the requirement and the maximal set of outputs from the function on that target
   */
  Iterator<Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>>> resolveFunction(String valueName, ComputationTarget target, ValueProperties constraints);

  /**
   * Gets the full set of resolution rules backing the resolver.
   * 
   * @return the full set of resolution rules, not null
   */
  Collection<ResolutionRule> getAllResolutionRules();

}
