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

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Visitor for processing resolution failure information. The basic implementation writes messages to a logger. Override these methods for more useful error reporting or handling.
 * 
 * @param <T> return type of the visit methods
 */
public abstract class ResolutionFailureVisitor<T> implements ResolutionFailureListener {

  private static final Logger s_logger = LoggerFactory.getLogger(ResolutionFailureVisitor.class);
  
  
  @Override
  public final void notifyFailure(ResolutionFailure resolutionFailure) {
    resolutionFailure.accept(this);
  }

  /**
   * A default instance for writing messages to a logger.
   */
  public static final ResolutionFailureVisitor<Void> DEFAULT_INSTANCE = new ResolutionFailureVisitor<Void>() {

    @Override
    protected Void visitCouldNotResolve(final ValueRequirement valueRequirement) {
      s_logger.info("Could not resolve {}", valueRequirement);
      return super.visitCouldNotResolve(valueRequirement);
    }

    @Override
    protected Void visitNoFunctions(final ValueRequirement valueRequirement) {
      s_logger.info("No functions available for {}", valueRequirement);
      return super.visitNoFunctions(valueRequirement);
    }

    @Override
    protected Void visitRecursiveRequirement(final ValueRequirement valueRequirement) {
      s_logger.info("Recursive requirement on {} for function(s) producing it", valueRequirement);
      return super.visitRecursiveRequirement(valueRequirement);
    }

    @Override
    protected Void visitUnsatisfied(final ValueRequirement valueRequirement) {
      s_logger.info("Unsatisfied requirement {}", valueRequirement);
      return super.visitUnsatisfied(valueRequirement);
    }

    @Override
    protected Void visitMarketDataMissing(final ValueRequirement valueRequirement) {
      s_logger.info("Market data missing to satisfy requirement {}", valueRequirement);
      return super.visitMarketDataMissing(valueRequirement);
    }

    @Override
    protected Void visitSuccessfulFunction(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
        final Map<ValueSpecification, ValueRequirement> satisfied) {
      s_logger.info("Applied {} for {}", function, valueRequirement);
      return super.visitSuccessfulFunction(valueRequirement, function, desiredOutput, satisfied);
    }

    @Override
    protected Void visitFailedFunction(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
        final Map<ValueSpecification, ValueRequirement> satisfied, final Set<ResolutionFailure> unsatisfied) {
      s_logger.info("Couldn't satisfy {} to produce {}", unsatisfied, desiredOutput);
      s_logger.info("Caused by:");
      return super.visitFailedFunction(valueRequirement, function, desiredOutput, satisfied, unsatisfied);
    }

    @Override
    protected Void visitGetAdditionalRequirementsFailed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
        final Map<ValueSpecification, ValueRequirement> requirements) {
      s_logger.info("getAdditionalRequirements method failed on {} with inputs {}", function, requirements);
      return super.visitGetAdditionalRequirementsFailed(valueRequirement, function, desiredOutput, requirements);
    }

    @Override
    protected Void visitGetResultsFailed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
        final Map<ValueSpecification, ValueRequirement> requirements) {
      s_logger.info("getResults method failed on {} with inputs {}", function, requirements);
      return super.visitGetResultsFailed(valueRequirement, function, desiredOutput, requirements);
    }

    @Override
    protected Void visitGetRequirementsFailed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput) {
      s_logger.info("getRequirements method failed on {} for {}", function, desiredOutput);
      return super.visitGetRequirementsFailed(valueRequirement, function, desiredOutput);
    }

    @Override
    protected Void visitLateResolutionFailure(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
        final Map<ValueSpecification, ValueRequirement> requirements) {
      s_logger.info("Provisional result {} not in function output after late resolution", desiredOutput);
      return super.visitLateResolutionFailure(valueRequirement, function, desiredOutput, requirements);
    }

    @Override
    protected Void visitBlacklistSuppressed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
        final Map<ValueSpecification, ValueRequirement> requirements) {
      s_logger.info("Function blacklist prevented resolution of {}", valueRequirement);
      return super.visitBlacklistSuppressed(valueRequirement, function, desiredOutput, requirements);
    }

  };

  protected T visitCouldNotResolve(final ValueRequirement valueRequirement) {
    return null;
  }

  protected T visitNoFunctions(final ValueRequirement valueRequirement) {
    return null;
  }

  protected T visitRecursiveRequirement(final ValueRequirement valueRequirement) {
    return null;
  }

  protected T visitUnsatisfied(final ValueRequirement valueRequirement) {
    return null;
  }

  protected T visitMarketDataMissing(final ValueRequirement valueRequirement) {
    return null;
  }

  protected T visitSuccessfulFunction(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> satisfied) {
    return null;
  }

  protected T visitFailedFunction(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> satisfied, final Set<ResolutionFailure> unsatisfied) {
    for (ResolutionFailure requirement : unsatisfied) {
      requirement.accept(this);
    }
    return null;
  }

  protected T visitFailedFunction(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> satisfied, final Set<ResolutionFailure> unsatisfied, final Set<ResolutionFailure> unsatisfiedAdditional) {
    if (unsatisfied.isEmpty()) {
      return visitFailedFunction(valueRequirement, function, desiredOutput, satisfied, unsatisfiedAdditional);
    } else if (unsatisfiedAdditional.isEmpty()) {
      return visitFailedFunction(valueRequirement, function, desiredOutput, satisfied, unsatisfied);
    } else {
      final Set<ResolutionFailure> combined = new HashSet<ResolutionFailure>(unsatisfied);
      combined.addAll(unsatisfiedAdditional);
      return visitFailedFunction(valueRequirement, function, desiredOutput, satisfied, combined);
    }
  }

  protected T visitFunction(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> satisfied, final Set<ResolutionFailure> unsatisfied, final Set<ResolutionFailure> unsatisfiedAdditional) {
    if (unsatisfied.isEmpty() && unsatisfiedAdditional.isEmpty()) {
      return visitSuccessfulFunction(valueRequirement, function, desiredOutput, satisfied);
    } else {
      return visitFailedFunction(valueRequirement, function, desiredOutput, satisfied, unsatisfied, unsatisfiedAdditional);
    }
  }

  protected T visitGetAdditionalRequirementsFailed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> requirements) {
    return null;
  }

  protected T visitGetResultsFailed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> requirements) {
    return null;
  }

  protected T visitGetRequirementsFailed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput) {
    return null;
  }

  protected T visitLateResolutionFailure(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> requirements) {
    return null;
  }

  protected T visitBlacklistSuppressed(final ValueRequirement valueRequirement, final String function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> requirements) {
    return null;
  }

}
