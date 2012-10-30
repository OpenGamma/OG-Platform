/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;

/**
 * Returns the same formatted value every time.
 */
/* package */ class FixedValueFormatter extends AbstractFormatter<Object> {

  private final Object _displayValue;
  private final Object _extendedDisplayValue;
  private final Object _historyValue;

  /**
   * @param displayValue The value that's always returned from {@link #formatForDisplay}
   * @param extendedDisplayValue The value that's always returned from {@link #formatForExpandedDisplay}
   * @param historyValue The value that's always returned from {@link #formatForHistory}
   */
  /* package */ FixedValueFormatter(String displayValue, String extendedDisplayValue, String historyValue) {
    _displayValue = displayValue;
    _extendedDisplayValue = extendedDisplayValue;
    _historyValue = historyValue;
  }

  @Override
  public Object formatForDisplay(Object value, ValueSpecification valueSpec) {
    return _displayValue;
  }

  @Override
  public Object formatForExpandedDisplay(Object value, ValueSpecification valueSpec) {
    return _extendedDisplayValue;
  }

  @Override
  public Object formatForHistory(Object history, ValueSpecification valueSpec) {
    return _historyValue;
  }

  @Override
  public FormatType getFormatForType() {
    return FormatType.PRIMITIVE;
  }
}
