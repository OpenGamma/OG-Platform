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
   * @param target Target to satisfy the requirement on
   * @return the function(s) found
   */
  Iterator<Pair<ParameterizedFunction, ValueSpecification>> resolveFunction(ValueRequirement requirement, ComputationTarget target);

  /**
   * Returns a full set of resolution rules backing the resolver.
   * 
   * @return the full set of resolution rules, not {@code null}
   */
  Collection<ResolutionRule> getAllResolutionRules();

}
