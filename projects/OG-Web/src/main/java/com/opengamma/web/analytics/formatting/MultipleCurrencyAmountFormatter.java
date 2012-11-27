/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
/* package */ class MultipleCurrencyAmountFormatter extends AbstractFormatter<MultipleCurrencyAmount> {

  /* package */ MultipleCurrencyAmountFormatter() {
    super(MultipleCurrencyAmount.class);
  }

  @Override
  public String formatCell(MultipleCurrencyAmount value, ValueSpecification valueSpec) {
    return "Vectors (" + value.size() + ")";
  }

  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_1D;
  }
}
