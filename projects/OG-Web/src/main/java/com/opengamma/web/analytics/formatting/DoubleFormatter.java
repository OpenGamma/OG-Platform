/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import java.math.BigDecimal;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class DoubleFormatter extends AbstractFormatter<Double> {

  private final BigDecimalFormatter _bigDecimalFormatter;

  DoubleFormatter(BigDecimalFormatter bigDecimalFormatter) {
    super(Double.class);
    ArgumentChecker.notNull(bigDecimalFormatter, "bigDecimalFormatter");
    _bigDecimalFormatter = bigDecimalFormatter;
    addFormatter(new Formatter<Double>(Format.HISTORY) {
      @Override
      Object format(Double value, ValueSpecification valueSpec, Object inlineKey) {
        return formatHistory(value, valueSpec);
      }
    });
    addFormatter(new Formatter<Double>(Format.EXPANDED) {
      @Override
      Object format(Double value, ValueSpecification valueSpec, Object inlineKey) {
        return formatExpanded(value, valueSpec);
      }
    });
  }

  @Override
  public String formatCell(Double value, ValueSpecification valueSpec, Object inlineKey) {
    BigDecimal bigDecimal = convertToBigDecimal(value);
    if (bigDecimal == null) {
      return Double.toString(value);
    } else {
      return _bigDecimalFormatter.formatCell(bigDecimal, valueSpec, inlineKey);
    }
  }

  private Object formatExpanded(Double value, ValueSpecification valueSpec) {
    BigDecimal bigDecimal = convertToBigDecimal(value);
    if (bigDecimal == null) {
      return Double.toString(value);
    } else {
      return _bigDecimalFormatter.format(bigDecimal, valueSpec, Format.EXPANDED, null);
    }
  }

  private Object formatHistory(Double history, ValueSpecification valueSpec) {
    BigDecimal bigDecimal = convertToBigDecimal(history);
    if (bigDecimal == null) {
      return null;
    } else {
      return _bigDecimalFormatter.format(bigDecimal, valueSpec, Format.HISTORY, null);
    }
  }

  @Override
  public DataType getDataType() {
    return DataType.DOUBLE;
  }

  private static BigDecimal convertToBigDecimal(Double value) {
    if (Double.isInfinite(value) || Double.isNaN(value)) {
      return null;
    } else {
      return new BigDecimal(value.toString());
    }
  }
}
