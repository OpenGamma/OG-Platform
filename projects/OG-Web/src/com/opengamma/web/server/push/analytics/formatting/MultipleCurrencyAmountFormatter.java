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
/* package */ class MultipleCurrencyAmountFormatter extends NoHistoryFormatter<MultipleCurrencyAmount> {

  @Override
  public String formatForDisplay(MultipleCurrencyAmount value, ValueSpecification valueSpec) {
    return "Vectors (" + value.size() + ")";
  }

  @Override
  public Object formatForExpandedDisplay(MultipleCurrencyAmount value, ValueSpecification valueSpec) {
    // TODO implement formatForExpandedDisplay()
    throw new UnsupportedOperationException("formatForExpandedDisplay not implemented");
  }

  @Override
  public FormatType getFormatType() {
    return FormatType.LABELLED_MATRIX_1D;
  }
}
