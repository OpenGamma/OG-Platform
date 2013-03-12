/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import com.opengamma.engine.ComputationTargetResolver;

/**
 * Compiles a string representation of an override operation to an {@link OverrideOperation}
 * instance.
 */
public interface OverrideOperationCompiler {

  /**
   * Compiles the given expression to an executable operation.
   * 
   * @param operation the textual representation of the operation, not null
   * @param resolver the resolver to use for any embedded or indirect references, not null
   * @return the executable operation, not null
   * @throws IllegalArgumentException if the expression is invalid
   */
  OverrideOperation compile(String operation, ComputationTargetResolver.AtVersionCorrection resolver);

}
