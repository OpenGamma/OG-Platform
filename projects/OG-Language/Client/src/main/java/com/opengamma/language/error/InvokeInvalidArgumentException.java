/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.error;

/**
 * Exception wrapper for {@link Constants.ERROR_INVALID_ARGUMENT}.
 */
public class InvokeInvalidArgumentException extends AbstractException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public InvokeInvalidArgumentException(final Throwable cause) {
    this(cause.getMessage(), cause);
  }

  public InvokeInvalidArgumentException(final String message) {
    super(Constants.ERROR_INVALID_ARGUMENT);
    setStringValue(message);
  }

  public InvokeInvalidArgumentException(final String message, final Throwable cause) {
    super(Constants.ERROR_INVALID_ARGUMENT, cause);
    setStringValue(message);
  }

  public InvokeInvalidArgumentException(final int parameterIndex, final Throwable cause) {
    this(parameterIndex, cause.getMessage(), cause);
  }

  public InvokeInvalidArgumentException(final int parameterIndex, final String message) {
    super(Constants.ERROR_INVALID_ARGUMENT);
    setIntValue(parameterIndex);
    setStringValue(message);
  }

  public InvokeInvalidArgumentException(final int parameterIndex, final String message, final Throwable cause) {
    super(Constants.ERROR_INVALID_ARGUMENT, cause);
    setIntValue(parameterIndex);
    setStringValue(message);
  }

  public Integer getParameterIndex() {
    return getValue().getIntValue();
  }

  public String getMessage() {
    return getStringValue();
  }

}
