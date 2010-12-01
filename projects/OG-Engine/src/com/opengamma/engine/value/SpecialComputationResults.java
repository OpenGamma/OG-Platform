/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import com.opengamma.core.position.Position;

/**
 * A set of values that can be returned by {@link ComputedValue#getValue()}
 * for certain common states of operation.
 */
public enum SpecialComputationResults {

  /**
   * The value simply wasn't computed during this pass.
   * This can be used to clear a value that was previously computed, but wasn't this time.
   */
  NOT_COMPUTED,
  /**
   * The particular computation isn't applicable to the specified {@link Position}.
   */
  NOT_APPLICABLE,
  /**
   * There was an error in performing the computation.
   */
  ERROR_IN_COMPUTATION;

}
