/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class ErrorInfo {

  private final String _message;
  private final Exception _exception;

  /* package */ ErrorInfo(Exception exception) {
    ArgumentChecker.notNull(exception, "exception");
    _message = exception.getMessage();
    _exception = exception;
  }

  /* package */ ErrorInfo(String message) {
    ArgumentChecker.notEmpty(message, "message");
    _message = message;
    _exception = null;
  }

  /**
   * @return The error message, possibly null
   */
  public String getMessage() {
    return _message;
  }

  /**
   * @return The exception that triggered the error, possibly null
   */
  public Exception getException() {
    return _exception;
  }
}
