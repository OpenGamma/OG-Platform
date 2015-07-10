/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.enginedebugger;

import java.util.Map;
import java.util.Set;

import com.opengamma.engine.depgraph.ResolutionFailure;
import com.opengamma.engine.depgraph.ResolutionFailureVisitor;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

final class ResolutionFailureChildCountVisitor extends ResolutionFailureVisitor<Integer> {
  
  private Integer getValueRequirementNode(final ValueRequirement valueRequirement) {
    return 1;
  }
  
  @Override
  protected Integer visitCouldNotResolve(final ValueRequirement valueRequirement) {
    return getValueRequirementNode(valueRequirement);
  }

  @Override
  protected Integer visitNoFunctions(final ValueRequirement valueRequirement) {
    return getValueRequirementNode(valueRequirement);
  }

  @Override
  protected Integer visitRecursiveRequirement(final ValueRequirement valueRequirement) {
    return getValueRequirementNode(valueRequirement);
  }

  @Override
  protected Integer visitUnsatisfied(final ValueRequirement valueRequirement) {
    return getValueRequirementNode(valueRequirement);
  }

  @Override
  protected Integer visitMarketDataMissing(final ValueRequirement valueRequirement) {
    return getValueRequirementNode(valueRequirement);
  }

  @Override
  protected Integer visitSuccessfulFunction(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> satisfied) {
    return 4;
  }

  @Override
  protected Integer visitFailedFunction(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> satisfied, final Set<ResolutionFailure> unsatisfied) {
    return 5;
  }

  @Override
  protected Integer visitGetAdditionalRequirementsFailed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> requirements) {
    return 4;
  }

  @Override
  protected Integer visitGetResultsFailed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> requirements) {
    return 4;
  }

  @Override
  protected Integer visitGetRequirementsFailed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput) {
    return 3;
  }

  @Override
  protected Integer visitLateResolutionFailure(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> requirements) {
    return 4;
  }

  @Override
  protected Integer visitBlacklistSuppressed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> requirements) {
    return 4;
  }

}
