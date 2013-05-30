/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.engine.calcnode.MissingValue;
import com.opengamma.util.ArgumentChecker;

/**
 * Error value returned to the client for values that can't be formatted.
 */
/*package*/ class MissingValueFormatter implements MissingValue {

  private final String _message;

  /*package*/ MissingValueFormatter(String message) {
    ArgumentChecker.notEmpty(message, "message");
    _message = message;
  }

  @Override
  public String toString() {
    return _message;
  }
  
}
