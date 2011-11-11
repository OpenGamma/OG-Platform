/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.value.ValueRequirement;

/**
 * Runtime exception thrown when a dependency graph cannot be constructed.
 * <p>
 * This is thrown during the creation of a dependency graph.
 * It indicates that the {@link FunctionDefinition}s available in the provided
 * {@link FunctionRepository} were insufficient to meet the requirements.
 */
public final class UnsatisfiableDependencyGraphException extends OpenGammaRuntimeException {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The requirement that could not be met.
   */
  private final ResolutionFailure _failure;

  /**
   * Creates an instance based on a resolution failure
   * 
   * @param failure the failure, not null
   */
  protected UnsatisfiableDependencyGraphException(final ResolutionFailure failure) {
    super(failure.getValueRequirement().toString());
    _failure = failure;
  }

  /**
   * Creates an instance based on a value requirement.
   * 
   * @param requirement the value requirement, not null
   */
  public UnsatisfiableDependencyGraphException(final ValueRequirement requirement) {
    this(ResolutionFailure.unsatisfied(requirement));
  }

  /**
   * Gets the value requirement that could not be met.
   * 
   * @return the value requirement, should not be null
   */
  public ValueRequirement getRequirement() {
    return getFailure().getValueRequirement();
  }

  /**
   * Gets the full failure description object.
   * 
   * @return the resolution description object
   */
  public ResolutionFailure getFailure() {
    return _failure;
  }

  // TODO: should have helper methods here to properly interrogate the failure object

}
