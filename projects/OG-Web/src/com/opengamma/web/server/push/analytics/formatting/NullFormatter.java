/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;

/**
 * Formats null values for display in the client.
 */
/* package */ class NullFormatter extends AbstractFormatter<Object> {

  /**
   * @param nullValue Not used
   * @param valueSpec Not used
   * @return An empty string
   */
  @Override
  public String formatForDisplay(Object nullValue, ValueSpecification valueSpec) {
    return "";
  }

  /**
   * @param nullValue Not used
   * @param valueSpec Not used
   * @return An empty string
   */
  @Override
  public Object formatForExpandedDisplay(Object nullValue, ValueSpecification valueSpec) {
    return "";
  }

  /**
   * @param nullHistoryValue Not used
   * @param valueSpec Not used
   * @return null
   */
  @Override
  public Object formatForHistory(Object nullHistoryValue, ValueSpecification valueSpec) {
    return null;
  }

  @Override
  public FormatType getFormatForType() {
    return FormatType.PRIMITIVE;
  }
}
