/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import java.math.BigDecimal;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;

/**
 *
 */
/* package */ class CurrencyAmountFormatter implements Formatter<CurrencyAmount> {

  private static final String NAME = "DOUBLE";

  private final BigDecimalFormatter _bigDecimalFormatter;

  /* package */ CurrencyAmountFormatter(BigDecimalFormatter bigDecimalFormatter) {
    ArgumentChecker.notNull(bigDecimalFormatter, "");
    _bigDecimalFormatter = bigDecimalFormatter;
  }

  @Override
  public String formatForDisplay(CurrencyAmount value, ValueSpecification valueSpec) {
    double amount = value.getAmount();
    BigDecimal bigDecimal = convertToBigDecimal(amount);
    if (bigDecimal == null) {
      return Double.toString(amount);
    } else {
      return value.getCurrency().getCode() + " " + _bigDecimalFormatter.formatForDisplay(bigDecimal, valueSpec);
    }
  }

  @Override
  public String formatForExpandedDisplay(CurrencyAmount value, ValueSpecification valueSpec) {
    double amount = value.getAmount();
    BigDecimal bigDecimal = convertToBigDecimal(amount);
    if (bigDecimal == null) {
      return Double.toString(amount);
    } else {
      return _bigDecimalFormatter.formatForExpandedDisplay(bigDecimal, valueSpec);
    }
  }

  /**
   * Returns the value's amount as a {@link BigDecimal} or {@code null} if the amount is infinite or not a number.
   * @param value The currency value, not null
   * @param valueSpec The specification that produced the value
   * @return The value's amount as a {@link BigDecimal} or {@code null} if the amount is infinite or not a number.
   */
  @Override
  public BigDecimal formatForHistory(CurrencyAmount value, ValueSpecification valueSpec) {
    double amount = value.getAmount();
    BigDecimal bigDecimal = convertToBigDecimal(amount);
    if (bigDecimal == null) {
      return null;
    } else {
      return _bigDecimalFormatter.formatForHistory(bigDecimal, valueSpec);
    }
  }

  @Override
  public String getName() {
    return NAME;
  }

  /**
   * @param value A double value, not null
   * @return The value converted to a {@link BigDecimal} or {@code null} if the value is infinite or not a number
   */
  private static BigDecimal convertToBigDecimal(Double value) {
    if (Double.isInfinite(value) || Double.isNaN(value)) {
      return null;
    } else {
      return new BigDecimal(value.toString());
    }
  }
}
