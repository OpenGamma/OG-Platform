/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import java.math.BigDecimal;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class DoubleFormatter implements Formatter<Double> {

  private final BigDecimalFormatter _bigDecimalFormatter;

  DoubleFormatter(BigDecimalFormatter bigDecimalFormatter) {
    ArgumentChecker.notNull(bigDecimalFormatter, "_bigDecimalFormatter");
    _bigDecimalFormatter = bigDecimalFormatter;
  }

  @Override
  public String formatForDisplay(Double value, ValueSpecification valueSpec) {
    BigDecimal bigDecimal = convertToBigDecimal(value);
    if (bigDecimal == null) {
      return Double.toString(value);
    } else {
      return _bigDecimalFormatter.formatForDisplay(bigDecimal, valueSpec);
    }
  }

  @Override
  public String formatForExpandedDisplay(Double value, ValueSpecification valueSpec) {
    BigDecimal bigDecimal = convertToBigDecimal(value);
    if (bigDecimal == null) {
      return Double.toString(value);
    } else {
      return _bigDecimalFormatter.formatForExpandedDisplay(bigDecimal, valueSpec);
    }
  }

  /**
   * Returns the value as a {@link BigDecimal} or {@code null} if it is infinite or not a number.
   * @param history The value, not null
   * @param valueSpec The specification that produced the value
   * @return The value as a {@link BigDecimal} or {@code null} if it is infinite or not a number.
   */
  @Override
  public BigDecimal formatForHistory(Double history, ValueSpecification valueSpec) {
    BigDecimal bigDecimal = convertToBigDecimal(history);
    if (bigDecimal == null) {
      return null;
    } else {
      return _bigDecimalFormatter.formatForHistory(bigDecimal, valueSpec);
    }
  }

  @Override
  public FormatType getFormatType() {
    return FormatType.DOUBLE;
  }

  private static BigDecimal convertToBigDecimal(Double value) {
    if (Double.isInfinite(value) || Double.isNaN(value)) {
      return null;
    } else {
      return new BigDecimal(value.toString());
    }
  }
}
