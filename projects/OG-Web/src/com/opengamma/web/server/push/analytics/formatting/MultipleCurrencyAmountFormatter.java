/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
/* package */ class MultipleCurrencyAmountFormatter implements Formatter<MultipleCurrencyAmount> {

  @Override
  public Object formatForDisplay(MultipleCurrencyAmount value, ValueSpecification valueSpec) {
    // TODO implement formatForDisplay()
    throw new UnsupportedOperationException("formatForDisplay not implemented");
  }

  @Override
  public Object formatForExpandedDisplay(MultipleCurrencyAmount value, ValueSpecification valueSpec) {
    // TODO implement formatForExpandedDisplay()
    throw new UnsupportedOperationException("formatForExpandedDisplay not implemented");
  }

  @Override
  public Object formatForHistory(MultipleCurrencyAmount history, ValueSpecification valueSpec) {
    // TODO implement formatForHistory()
    throw new UnsupportedOperationException("formatForHistory not implemented");
  }

  @Override
  public FormatType getFormatType() {
    // TODO implement getFormatType()
    throw new UnsupportedOperationException("getFormatType not implemented");
  }
}
