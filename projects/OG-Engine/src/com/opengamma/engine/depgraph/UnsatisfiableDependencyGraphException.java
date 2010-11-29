/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.FunctionRepository;

/**
 * Thrown when a desired dependency graph cannot be constructed from
 * the {@link FunctionDefinition}s available in the provided
 * {@link FunctionRepository}.
 *
 * @author kirk
 */
public class UnsatisfiableDependencyGraphException extends
    OpenGammaRuntimeException {
  
  public UnsatisfiableDependencyGraphException(String message) {
    super(message);
  }
  
  public UnsatisfiableDependencyGraphException(String message, Throwable cause) {
    super(message, cause);
  }
  
  // TODO kirk 2009-09-04 -- Add in all the various missing dependencies.

}
