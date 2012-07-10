/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
/* package */ class FixedStringFormatter implements Formatter<Object> {

  private final String _value;

  /* package */ FixedStringFormatter(String value) {
    _value = value;
  }

  @Override
  public String formatForDisplay(Object value, ValueSpecification valueSpec) {
    return _value;
  }

  @Override
  public String formatForExpandedDisplay(Object value, ValueSpecification valueSpec) {
    return _value;
  }

  @Override
  public String formatForHistory(Object history, ValueSpecification valueSpec) {
    return _value;
  }

  @Override
  public FormatType getFormatType() {
    return FormatType.PRIMITIVE;
  }
}
