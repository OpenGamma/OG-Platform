/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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

final class ResolutionFailureNameVisitor extends ResolutionFailureVisitor<Object> {
  static final String COULD_NOT_RESOLVE = "Could not resolve";
  static final String NO_FUNCTIONS = "No functions";
  static final String RECURSIVE_REQUIREMENT = "Recursive Requirement";
  static final String UNSATISFIED = "Unsatisfied";
  static final String MISSING_MARKET_DATA = "Missing Market Data";
  static final String SUCCESSFUL_FUNCTION = "Successful Function";
  static final String FAILED_FUNCTION = "Failed Function";
  static final String GET_ADDITIONAL_REQUIREMENTS_FAILED = "getAdditionalRequirements() failed";
  static final String GET_RESULTS_FAILED = "getResults() failed";
  static final String GET_REQUIREMENTS_FAILED = "getRequirements() failed";
  static final String LATE_RESOLUTION_FAILURE = "Late resolution failure";
  static final String BLACKLIST_SUPPRESSED = "Blacklist supressed";
  @Override
  protected Object visitCouldNotResolve(final ValueRequirement valueRequirement) {
    return COULD_NOT_RESOLVE;
  }

  @Override
  protected String visitNoFunctions(final ValueRequirement valueRequirement) {
    return NO_FUNCTIONS;
  }

  @Override
  protected String visitRecursiveRequirement(final ValueRequirement valueRequirement) {
    return RECURSIVE_REQUIREMENT;
  }

  @Override
  protected String visitUnsatisfied(final ValueRequirement valueRequirement) {
    return UNSATISFIED;
  }

  @Override
  protected String visitMarketDataMissing(final ValueRequirement valueRequirement) {
    return MISSING_MARKET_DATA;
  }

  @Override
  protected String visitSuccessfulFunction(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> satisfied) {
    return SUCCESSFUL_FUNCTION;
  }

  @Override
  protected String visitFailedFunction(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> satisfied, final Set<ResolutionFailure> unsatisfied) {
    return FAILED_FUNCTION;
  }

  @Override
  protected String visitGetAdditionalRequirementsFailed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> requirements) {
    return GET_ADDITIONAL_REQUIREMENTS_FAILED;
  }

  @Override
  protected String visitGetResultsFailed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> requirements) {
    return GET_RESULTS_FAILED;
  }

  @Override
  protected String visitGetRequirementsFailed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput) {
    return GET_REQUIREMENTS_FAILED;
  }

  @Override
  protected String visitLateResolutionFailure(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> requirements) {
    return LATE_RESOLUTION_FAILURE;
  }

  @Override
  protected String visitBlacklistSuppressed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> requirements) {
    return BLACKLIST_SUPPRESSED;
  }
}
