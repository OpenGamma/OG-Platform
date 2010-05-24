/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class DefaultFunctionResolver implements FunctionResolver {
  
  private FunctionRepository _functionRepository;

  public DefaultFunctionResolver(FunctionRepository functionRepository) {
    _functionRepository = functionRepository;
  }
  
  private FunctionRepository getFunctionRepository() {
    return _functionRepository;
  }

  @Override
  public Pair<FunctionDefinition, ValueSpecification> resolveFunction(
      FunctionCompilationContext context, ComputationTarget target, ValueRequirement requirement) {
    for (FunctionDefinition function : getFunctionRepository().getAllFunctions()) {
      if (function instanceof FunctionDefinition) {
        FunctionDefinition newFunction = (FunctionDefinition) function;
        if (!newFunction.canApplyTo(context, target)) {
          continue;
        }
        Set<ValueSpecification> resultSpecs = newFunction.getResults(context, target);
        for (ValueSpecification resultSpec : resultSpecs) {
          if (ObjectUtils.equals(resultSpec.getRequirementSpecification(), requirement)) {
            return Pair.of(newFunction, resultSpec);
          }
        }
      }
      
    }
    return null;
  }

}
