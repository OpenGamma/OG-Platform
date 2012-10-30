/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.error;

import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;

/**
 * Base class for exceptions that should be propogated to the client.
 */
public abstract class AbstractException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private final Value _value;

  protected AbstractException(final int err) {
    _value = ValueUtils.ofError(err);
  }

  protected AbstractException(final int err, final Throwable cause) {
    super(cause);
    _value = ValueUtils.ofError(err);
  }

  protected final void setIntValue(final int intValue) {
    _value.setIntValue(intValue);
  }

  protected final int getIntValue() {
    return _value.getIntValue();
  }

  protected final void setStringValue(final String stringValue) {
    _value.setStringValue(stringValue);
  }

  protected final String getStringValue() {
    return _value.getStringValue();
  }

  public final Value getValue() {
    return _value;
  }

  public final int getError() {
    return _value.getErrorValue();
  }

}
