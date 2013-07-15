/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.digest;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionCompilationContext;

/**
 * Dummy implementation that never returns a digest, suppressing the algorithm.
 */
public final class NoTargetDigests implements TargetDigests {

  @Override
  public Object getDigest(final FunctionCompilationContext context, final ComputationTargetSpecification target) {
    return null;
  }

}
