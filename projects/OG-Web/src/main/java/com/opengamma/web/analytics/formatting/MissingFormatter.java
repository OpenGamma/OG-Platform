/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.engine.view.calcnode.MissingInput;
import com.opengamma.util.ArgumentChecker;

/**
 * Error value returned to the client for values that can't be formatted.
 */
/* package */ class MissingFormatter implements MissingInput {

  private final String _message;

  /* package */ MissingFormatter(String message) {
    ArgumentChecker.notEmpty(message, "message");
    _message = message;
  }

  @Override
  public String toString() {
    return _message;
  }
}
