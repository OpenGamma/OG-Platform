/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.error;

/**
 * Exception wrapper for {@link Constants.ERROR_PARAMETER_CONVERSION}.
 */
public class InvokeParameterConversionException extends AbstractException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public InvokeParameterConversionException(final int parameterIndex) {
    this(parameterIndex, (String) null);
  }

  public InvokeParameterConversionException(final int parameterIndex, final Throwable cause) {
    this(parameterIndex, cause.getMessage(), cause);
  }

  public InvokeParameterConversionException(final int parameterIndex, final String message) {
    super(Constants.ERROR_PARAMETER_CONVERSION);
    setIntValue(parameterIndex);
    setStringValue(message);
  }

  public InvokeParameterConversionException(final int parameterIndex, final String message, final Throwable cause) {
    super(Constants.ERROR_PARAMETER_CONVERSION, cause);
    setIntValue(parameterIndex);
    setStringValue(message);
  }

  public int getParameterIndex() {
    return getIntValue();
  }

  public String getMessage() {
    return getStringValue();
  }

}
