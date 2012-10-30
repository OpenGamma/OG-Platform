/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.error;

/**
 * Exception wrapper for {@link Constants.ERROR_RESULT_CONVERSION}.
 */
public class InvokeResultConversionException extends AbstractException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public InvokeResultConversionException() {
    this(0, (String) null);
  }

  public InvokeResultConversionException(final Throwable cause) {
    this(0, cause.getMessage(), cause);
  }

  public InvokeResultConversionException(final String message) {
    this(0, message);
  }

  public InvokeResultConversionException(final String message, final Throwable cause) {
    this(0, message, cause);
  }

  public InvokeResultConversionException(final int resultIndex) {
    this(resultIndex, (String) null);
  }

  public InvokeResultConversionException(final int resultIndex, final Throwable cause) {
    this(resultIndex, cause.getMessage(), cause);
  }

  public InvokeResultConversionException(final int resultIndex, final String message) {
    super(Constants.ERROR_RESULT_CONVERSION);
    setIntValue(resultIndex);
    setStringValue(message);
  }

  public InvokeResultConversionException(final int resultIndex, final String message, final Throwable cause) {
    super(Constants.ERROR_PARAMETER_CONVERSION, cause);
    setIntValue(resultIndex);
    setStringValue(message);
  }

  public int getResultIndex() {
    return getIntValue();
  }

  public String getMessage() {
    return getStringValue();
  }

}
