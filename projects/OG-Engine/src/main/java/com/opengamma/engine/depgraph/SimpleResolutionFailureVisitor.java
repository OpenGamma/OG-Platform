/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 */
public class SimpleResolutionFailureVisitor extends ResolutionFailureVisitor<List<ResolutionFailure>> {

  @Override
  protected List<ResolutionFailure> visitCouldNotResolve(ValueRequirement valueRequirement) {
    return Collections.singletonList(ResolutionFailureImpl.couldNotResolve(valueRequirement));
  }

  @Override
  protected List<ResolutionFailure> visitNoFunctions(ValueRequirement valueRequirement) {
    return Collections.singletonList(ResolutionFailureImpl.noFunctions(valueRequirement));
  }

  @Override
  protected List<ResolutionFailure> visitRecursiveRequirement(ValueRequirement valueRequirement) {
    return Collections.singletonList(ResolutionFailureImpl.recursiveRequirement(valueRequirement));
  }

  @Override
  protected List<ResolutionFailure> visitUnsatisfied(ValueRequirement valueRequirement) {
    return Collections.singletonList(ResolutionFailureImpl.unsatisfied(valueRequirement));
  }

  @Override
  protected List<ResolutionFailure> visitMarketDataMissing(ValueRequirement valueRequirement) {
    return Collections.singletonList(ResolutionFailureImpl.marketDataMissing(valueRequirement));
  }

  @Override
  protected List<ResolutionFailure> visitSuccessfulFunction(ValueRequirement valueRequirement, String function, ValueSpecification desiredOutput, Map<ValueSpecification, ValueRequirement> satisfied) {
    return Collections.emptyList(); //TODO is this correct?
  }

  @Override
  //TODO not on fudge builder visitor?
  protected List<ResolutionFailure> visitFailedFunction(ValueRequirement valueRequirement, String function, ValueSpecification desiredOutput, Map<ValueSpecification, ValueRequirement> satisfied,
      Set<ResolutionFailure> unsatisfied) {
    return ImmutableList.copyOf(unsatisfied);
  }

  @Override
  protected List<ResolutionFailure> visitFailedFunction(ValueRequirement valueRequirement, String function, ValueSpecification desiredOutput, Map<ValueSpecification, ValueRequirement> satisfied,
      Set<ResolutionFailure> unsatisfied, Set<ResolutionFailure> unsatisfiedAdditional) {
    return ImmutableList.copyOf(Iterables.concat(unsatisfied, unsatisfiedAdditional));
  }

  @Override
  protected List<ResolutionFailure> visitFunction(ValueRequirement valueRequirement, String function, ValueSpecification desiredOutput, Map<ValueSpecification, ValueRequirement> satisfied,
      Set<ResolutionFailure> unsatisfied, Set<ResolutionFailure> unsatisfiedAdditional) {
    return ImmutableList.copyOf(Iterables.concat(unsatisfied, unsatisfiedAdditional));
  }

  @Override
  protected List<ResolutionFailure> visitGetAdditionalRequirementsFailed(ValueRequirement valueRequirement, String function, ValueSpecification desiredOutput,
      Map<ValueSpecification, ValueRequirement> requirements) {
    return Collections.emptyList(); //TODO is this correct?
  }

  @Override
  protected List<ResolutionFailure> visitGetResultsFailed(ValueRequirement valueRequirement, String function, ValueSpecification desiredOutput, 
      Map<ValueSpecification, ValueRequirement> requirements) {
    return Collections.emptyList(); //TODO is this correct?
  }

  @Override
  protected List<ResolutionFailure> visitGetRequirementsFailed(ValueRequirement valueRequirement, String function, ValueSpecification desiredOutput) {
    return Collections.emptyList(); //TODO is this correct?
  }

  @Override
  protected List<ResolutionFailure> visitLateResolutionFailure(ValueRequirement valueRequirement, String function, ValueSpecification desiredOutput, 
      Map<ValueSpecification, ValueRequirement> requirements) {
    return Collections.emptyList(); //TODO is this correct?
  }

  @Override
  protected List<ResolutionFailure> visitBlacklistSuppressed(ValueRequirement valueRequirement, String function, ValueSpecification desiredOutput,
      Map<ValueSpecification, ValueRequirement> requirements) {
    return Collections.emptyList(); //TODO is this correct?
  }

}
