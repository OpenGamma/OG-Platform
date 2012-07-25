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
/* package */ class NullFormatter implements Formatter<Object> {

  @Override
  public String formatForDisplay(Object nullValue, ValueSpecification valueSpec) {
    return null;
  }

  @Override
  public Object formatForExpandedDisplay(Object nullValue, ValueSpecification valueSpec) {
    return null;
  }

  @Override
  public Object formatForHistory(Object nullHistoryValue, ValueSpecification valueSpec) {
    return null;
  }

  @Override
  public FormatType getFormatType() {
    return FormatType.PRIMITIVE;
  }
}
