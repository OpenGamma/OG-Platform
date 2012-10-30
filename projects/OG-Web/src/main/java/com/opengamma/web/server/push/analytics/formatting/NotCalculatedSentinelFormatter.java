/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.NotCalculatedSentinel;

/**
 * Formats instances of {@link NotCalculatedSentinel} which are placeholders in the analytics results for values
 * that couldn't be calculated.
 */
public class NotCalculatedSentinelFormatter extends AbstractFormatter<NotCalculatedSentinel> {

  /**
   * @param value The value
   * @param valueSpec Its specification
   * @return Description of the value (which is the reason the calculation failed)
   */
  @Override
  public Object formatForDisplay(NotCalculatedSentinel value, ValueSpecification valueSpec) {
    return value.toString();
  }

  /**
   * @param value The value
   * @param valueSpec Its specification
   * @return Description of the value (which is the reason the calculation failed)
   */
  @Override
  public Object formatForExpandedDisplay(NotCalculatedSentinel value, ValueSpecification valueSpec) {
    return value.toString();
  }

  /**
   * @param history The value
   * @param valueSpec Its specification
   * @return null
   */
  @Override
  public Object formatForHistory(NotCalculatedSentinel history, ValueSpecification valueSpec) {
    return null;
  }

  @Override
  public FormatType getFormatForType() {
    return FormatType.PRIMITIVE;
  }
}
