/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import com.opengamma.engine.calcnode.MissingValue;

/**
 * Instances of this class are saved in the computation cache for outputs that are not calculated because of an error.
 * <p>
 * Exact details of the failure can be found from the calculation job result that is routed back to the view processor.
 */
public enum MissingOutput implements MissingValue {

  /**
   * Value used in place of the output to indicate that a function was not executed because of missing inputs.
   */
  MISSING_INPUTS("Missing inputs"),

  /**
   * Value used in place of the output to indicate that an error occurred while executing a function.
   */
  EVALUATION_ERROR("Evaluation error"),

  /**
   * Value used in place of the output where blacklist suppression or a previous failure prevented the function from
   * being executed.
   */
  SUPPRESSED("Suppressed");

  private final String _reason;

  private MissingOutput(final String reason) {
    _reason = reason;
  }

  public String toString() {
    return _reason;
  }

}
