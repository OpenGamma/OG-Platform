/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;

/**
 * Abstract base class for formatters that don't support history values.
 */
/* package */ abstract class NoHistoryFormatter<T> implements Formatter<T> {

  /**
   * Throws an UnsupportedOperationException.
   * @param history Not used
   * @param valueSpec Not used
   * @return Nothing
   */
  @Override
  public Object formatForHistory(T history, ValueSpecification valueSpec) {
    throw new UnsupportedOperationException("history not supported for format " + getFormatType().name());
  }
}
