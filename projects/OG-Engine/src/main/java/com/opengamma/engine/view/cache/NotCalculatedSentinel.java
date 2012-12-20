/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import com.opengamma.engine.view.calcnode.MissingInput;

/**
 * Instances of this class are saved in the computation cache for outputs that aren't calculated because of an error. Exact details of the failure can be found from the calculation job result that is
 * routed back to the view processor
 */
public enum NotCalculatedSentinel implements MissingInput {

  /**
   * Placeholder for functions that weren't executed because of missing inputs.
   */
  MISSING_INPUTS("Missing inputs"),

  /**
   * Placeholder for functions that were executed but failed to produce one or more results.
   */
  EVALUATION_ERROR("Evaluation error"),

  /**
   * Placeholder for functions that weren't executed because of blacklist suppression or a failure on a previous cycle.
   */
  SUPPRESSED("Suppressed");

  private final String _reason;

  private NotCalculatedSentinel(final String reason) {
    _reason = reason;
  }

  public String toString() {
    return _reason;
  }

}
