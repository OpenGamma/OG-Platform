/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Used when a conversion is not possible.
 */
public class InvalidConversionException extends IllegalArgumentException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private final Object _value;
  private final JavaTypeInfo<?> _type;

  public InvalidConversionException(final Object value, final JavaTypeInfo<?> type) {
    super();
    _value = value;
    _type = type;
  }

  public Object getValue() {
    return _value;
  }

  public JavaTypeInfo<?> getTargetType() {
    return _type;
  }

  @Override
  public String getMessage() {
    return "Could not convert from " + _value + " to " + _type;
  }

}
