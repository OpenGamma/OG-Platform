/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.NotCalculatedSentinel;

/**
 * Formats instances of {@link NotCalculatedSentinel} which are placeholders in the analytics results for values
 * that couldn't be calculated.
 */
/* package */ class NotCalculatedSentinelFormatter extends AbstractFormatter<NotCalculatedSentinel> {

  /* package */ NotCalculatedSentinelFormatter() {
    super(NotCalculatedSentinel.class);
    addFormatter(new Formatter<NotCalculatedSentinel>(Format.HISTORY) {
      @Override
      Object format(NotCalculatedSentinel value, ValueSpecification valueSpec) {
        return null;
      }
    });
    addFormatter(new Formatter<NotCalculatedSentinel>(Format.EXPANDED) {
      @Override
      Object format(NotCalculatedSentinel value, ValueSpecification valueSpec) {
        return value.toString();
      }
    });
  }

  /**
   * @param value The value
   * @param valueSpec Its specification
   * @return Description of the value (which is the reason the calculation failed)
   */
  @Override
  public Object formatCell(NotCalculatedSentinel value, ValueSpecification valueSpec) {
    return value.toString();
  }

  @Override
  public DataType getDataType() {
    return DataType.PRIMITIVE;
  }
}
