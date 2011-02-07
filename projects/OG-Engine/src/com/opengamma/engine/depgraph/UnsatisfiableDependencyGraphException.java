/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.value.ValueRequirement;

/**
 * Thrown when a desired dependency graph cannot be constructed from
 * the {@link FunctionDefinition}s available in the provided
 * {@link FunctionRepository}.
 *
 * @author kirk
 */
public class UnsatisfiableDependencyGraphException extends OpenGammaRuntimeException {

  private final ValueRequirement _requirement;

  public UnsatisfiableDependencyGraphException(final ValueRequirement requirement) {
    super("");
    _requirement = requirement;
  }

  public UnsatisfiableDependencyGraphException(final ValueRequirement requirement, final Throwable cause) {
    super("", cause);
    _requirement = requirement;
  }

  public ValueRequirement getRequirement() {
    return _requirement;
  }

  @Override
  public String getMessage() {
    return "Couldn't satisfy " + getRequirement();
  }

  // TODO kirk 2009-09-04 -- Add in all the various missing dependencies.

}
