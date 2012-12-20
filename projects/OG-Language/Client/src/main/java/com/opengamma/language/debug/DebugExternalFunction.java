/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.debug;

import com.opengamma.lang.annotation.ExternalFunction;

/**
 * Trivial external functions for debugging/testing.
 */
public class DebugExternalFunction {

  @ExternalFunction
  public String echo(final int i, final String j) {
    return "i=" + i + ", j=" + j;
  }

}
