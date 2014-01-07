/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.util.ArgumentChecker;

/**
 * Information about an error that occurred in the server.
 */
public final class ErrorInfo {

  private final String _message;
  private final Throwable _throwable;
  private final long _id;

  public ErrorInfo(long id, Throwable throwable) {
    _id = id;
    ArgumentChecker.notNull(throwable, "throwable");
    _message = throwable.getMessage();
    _throwable = throwable;
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
  public Throwable getThrowable() {
    return _throwable;
  }

  /**
   * @return The unique ID of the error (unique within the view)
   */
  public long getId() {
    return _id;
  }

  @Override
  public String toString() {
    return "ErrorInfo [_message='" + _message + "', _throwable=" + _throwable + "]";
  }
}
