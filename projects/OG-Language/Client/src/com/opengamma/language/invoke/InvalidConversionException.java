/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtil;
import com.opengamma.language.Value;
import com.opengamma.language.ValueUtil;
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

  /**
   * Tidy up the raw values. This string is likely to be visible to a user so should make sense to them.
   */
  public String getClientMessage() {
    final StringBuilder sb = new StringBuilder();
    sb.append("Could not convert ");
    if (_value instanceof Value) {
      sb.append(ValueUtil.toString((Value) _value, true));
    } else if (_value instanceof Data) {
      sb.append(DataUtil.toString((Data) _value, true));
    } else {
      sb.append(_value.toString());
    }
    sb.append(" to ").append(_type.toClientString());
    return sb.toString();
  }

}
