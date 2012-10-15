/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collection;
import java.util.Map;

import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Abstraction of a resolution failure.
 */
public abstract class ResolutionFailure implements Cloneable {

  /**
   * Standard status constants relating to the method calls available here and the callbacks in the visitor.
   */
  public static enum Status {
    /**
     * An additional requirement (requested by {@link CompiledFunctionDefinition#getAdditionalRequirements}) could not be resolved.
     */
    ADDITIONAL_REQUIREMENT,
    /**
     * The {@link ComputationTargetResolver} could not resolve the target.
     */
    COULD_NOT_RESOLVE,
    /**
     * A problem occurred with the call to, or result from, {@link CompiledFunctionDefinition#getAdditionalRequirements}.
     */
    GET_ADDITIONAL_REQUIREMENTS_FAILED,
    /**
     * A problem occurred with the call to, or result from, {@link CompiledFunctionDefinition#getResults}.
     */
    GET_RESULTS_FAILED,
    /**
     * A problem occurred with the call to, or result from, {@link CompiledFunctionDefinition#getRequirements}.
     */
    GET_REQUIREMENTS_FAILED,
    /**
     * A problem occurred with the call to, or result from, the second {@link CompiledFunctionDefinition#getResults} method.
     */
    LATE_RESOLUTION_FAILURE,
    /**
     * The {@link MarketDataAvailabilityProvider} requested that the requirement not be satisfied as market data is explicitly absent.
     */
    MARKET_DATA_MISSING,
    /**
     * No functions could be found producing outputs that satisfy the requirement.
     */
    NO_FUNCTIONS,
    /**
     * The explored route is not valid as it would introduce a loop into the dependency graph.
     */
    RECURSIVE_REQUIREMENT,
    /**
     * Miscellaneous inability to satisfy the requirement. No further information available.
     */
    UNSATISFIED,
    /**
     * A blacklist entry has suppressed resolution of the requirement.
     */
    SUPPRESSED
  }

  /* package */ResolutionFailure() {
  }

  // Construction

  protected abstract ResolutionFailure additionalRequirement(final ValueRequirement valueRequirement, final ResolutionFailure failure);

  protected abstract ResolutionFailure requirement(final ValueRequirement valueRequirement, final ResolutionFailure failure);

  protected abstract ResolutionFailure requirements(final Map<ValueSpecification, ValueRequirement> available);

  protected abstract ResolutionFailure getResultsFailed();

  protected abstract ResolutionFailure getAdditionalRequirementsFailed();

  protected abstract ResolutionFailure lateResolutionFailure();

  protected abstract ResolutionFailure getRequirementsFailed();

  protected abstract ResolutionFailure checkFailure(final ValueRequirement valueRequirement);

  protected abstract ResolutionFailure suppressed();

  // Query

  public abstract ValueRequirement getValueRequirement();

  public abstract <T> Collection<T> accept(final ResolutionFailureVisitor<T> visitor);

  // Composition

  /**
   * Merge the causes of failure from the other into this.
   * 
   * @param failure cause of failure
   */
  protected abstract void merge(final ResolutionFailure failure);

  // Misc

  @Override
  public abstract String toString();

  @Override
  public abstract Object clone();

  /**
   * Tests this resolution failure object with another for equality. Note that the caller must ensure that the monitor for both is held, or a suitable exclusion lock is held at an outer level.
   * 
   * @param obj object to compare to
   * @return true if the objects are equal
   */
  @Override
  public abstract boolean equals(final Object obj);

  @Override
  public abstract int hashCode();

}
