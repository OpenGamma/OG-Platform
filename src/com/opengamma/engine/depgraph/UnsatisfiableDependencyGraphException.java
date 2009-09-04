/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Thrown when a desired dependency graph cannot be constructed from
 * the {@link AnalyticFunction}s available in the provided
 * {@link AnalyticFunctionRepository}.
 *
 * @author kirk
 */
public class UnsatisfiableDependencyGraphException extends
    OpenGammaRuntimeException {
  
  public UnsatisfiableDependencyGraphException(String message) {
    super(message);
  }
  
  // TODO kirk 2009-09-04 -- Add in all the various missing dependencies.

}
