/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.error;

/**
 * Exception wrapper for {@link Constants.ERROR_INTERNAL}. This should only be
 * used when none of the other exception constants are more appropriate.
 */
public class InvokeInternalException extends AbstractException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public InvokeInternalException(final Throwable cause) {
    this(cause.getMessage(), cause);
  }

  public InvokeInternalException(final String message) {
    super(Constants.ERROR_INTERNAL);
    setStringValue(message);
  }

  public InvokeInternalException(final String message, final Throwable cause) {
    super(Constants.ERROR_INTERNAL, cause);
    setStringValue(message);
  }

  public String getMessage() {
    return getStringValue();
  }

}
