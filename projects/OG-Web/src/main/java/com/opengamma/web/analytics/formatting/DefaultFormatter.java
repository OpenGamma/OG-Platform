/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
/* package */ class DefaultFormatter extends AbstractFormatter<Object> {

  /** Maximum string length for values that are sent to the grid. */
  private static final int MAX_VALUE_LENGTH = 100;

  /* package */ DefaultFormatter() {
    super(Object.class);
  }

  @Override
  public String formatCell(Object value, ValueSpecification valueSpec, Object inlineKey) {
    return trim(value.toString());
  }

  @Override
  public DataType getDataType() {
    return DataType.STRING;
  }

  /**
   * If a string is shorter than {@link #MAX_VALUE_LENGTH} it is returned, otherwise it is trimmed and
   * "..." is appended.
   * @param str The string to be trimmed.
   * @return The trimmed string.
   */
  private static String trim(String str) {
    if (str.length() < MAX_VALUE_LENGTH) {
      return str;
    } else {
      return str.substring(0, MAX_VALUE_LENGTH) + "...";
    }
  }
}
