/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Visitor for processing resolution failure information. The basic implementation writes messages to a logger. Override these
 * methods for more useful error reporting.
 */
public abstract class ResolutionFailureVisitor {

  private static final Logger s_logger = LoggerFactory.getLogger(ResolutionFailureVisitor.class);

  /**
   * A default instance for writing messages to a logger.
   */
  public static final ResolutionFailureVisitor DEFAULT_INSTANCE = new ResolutionFailureVisitor() {
  };

  protected void visitCouldNotResolve(final ValueRequirement valueRequirement) {
    s_logger.warn("Could not resolve {}", valueRequirement);
  }

  protected void visitNoFunctions(final ValueRequirement valueRequirement) {
    s_logger.warn("No functions available for {}", valueRequirement);
  }

  protected void visitRecursiveRequirement(final ValueRequirement valueRequirement) {
    s_logger.warn("Recursive requirement on {} for function(s) producing it", valueRequirement);
  }

  protected void visitUnsatisfied(final ValueRequirement valueRequirement) {
    s_logger.warn("Unsatisfied requirement {}", valueRequirement);
  }

  protected void visitResolvedValue(final ValueRequirement valueRequirement, final ResolvedValue value) {
    s_logger.info("Resolved {} to {}", valueRequirement, value);
  }

  protected void visitSuccessfulFunction(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> satisfied) {
    s_logger.info("Applied {} for {}", function, valueRequirement);
  }

  protected void visitFailedFunction(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> satisfied, final Set<ResolutionFailure> unsatisfied) {
    s_logger.warn("Couldn't satisfy {} to produce {}", unsatisfied, desiredOutput);
    s_logger.info("Caused by:");
    for (ResolutionFailure requirement : unsatisfied) {
      requirement.accept(this);
    }
  }

  protected void visitFailedFunction(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> satisfied, final Set<ResolutionFailure> unsatisfied, final Set<ResolutionFailure> unsatisfiedAdditional) {
    if (unsatisfied.isEmpty()) {
      visitFailedFunction(valueRequirement, function, desiredOutput, satisfied, unsatisfiedAdditional);
    } else if (unsatisfiedAdditional.isEmpty()) {
      visitFailedFunction(valueRequirement, function, desiredOutput, satisfied, unsatisfied);
    } else {
      final Set<ResolutionFailure> combined = new HashSet<ResolutionFailure>(unsatisfied);
      combined.addAll(unsatisfiedAdditional);
      visitFailedFunction(valueRequirement, function, desiredOutput, satisfied, combined);
    }
  }

  protected void visitFunction(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> satisfied, final Set<ResolutionFailure> unsatisfied, final Set<ResolutionFailure> unsatisfiedAdditional) {
    if (unsatisfied.isEmpty() && unsatisfiedAdditional.isEmpty()) {
      visitSuccessfulFunction(valueRequirement, function, desiredOutput, satisfied);
    } else {
      visitFailedFunction(valueRequirement, function, desiredOutput, satisfied, unsatisfied, unsatisfiedAdditional);
    }
  }

  protected void visitGetAdditionalRequirementsFailed(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> requirements) {
    s_logger.error("getAdditionalRequirements method failed on {} with inputs {}", function, requirements);
  }

  protected void visitGetResultsFailed(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification desiredOutput) {
    s_logger.error("getResults method failed on {}", function);
  }

  protected void visitGetRequirementsFailed(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification desiredOutput) {
    s_logger.error("getRequirements method failed on {} for {}", function, desiredOutput);
  }

  protected void visitLateResolutionFailure(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> requirements) {
    s_logger.warn("Provisional result {} not in function output after late resolution", desiredOutput);
  }

}
