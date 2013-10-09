/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.lookup.FXAmounts;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class FXAmountsFormatter extends AbstractFormatter<FXAmounts> {

  private final DoubleFormatter _doubleFormatter;

  /* package */ FXAmountsFormatter(DoubleFormatter doubleFormatter) {
    super(FXAmounts.class);
    ArgumentChecker.notNull(doubleFormatter, "doubleFormatter");
    _doubleFormatter = doubleFormatter;
  }

  @Override
  public Object formatCell(FXAmounts amounts, ValueSpecification valueSpec, Object inlineKey) {
    return _doubleFormatter.formatCell(amounts.getBaseAmount(), valueSpec, inlineKey) + " " + amounts.getBaseCurrency() + " / " +
        _doubleFormatter.formatCell(amounts.getCounterAmount(), valueSpec, inlineKey) + " " + amounts.getCounterCurrency();
  }

  @Override
  public DataType getDataType() {
    return DataType.STRING;
  }
}
