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
/* package */ class DoubleFormatter extends AbstractFormatter<Double> {

  private final BigDecimalFormatter _bigDecimalFormatter;

  DoubleFormatter(BigDecimalFormatter bigDecimalFormatter) {
    super(Double.class);
    ArgumentChecker.notNull(bigDecimalFormatter, "_bigDecimalFormatter");
    _bigDecimalFormatter = bigDecimalFormatter;
  }

  @Override
  public String formatCell(Double value, ValueSpecification valueSpec) {
    BigDecimal bigDecimal = convertToBigDecimal(value);
    if (bigDecimal == null) {
      return Double.toString(value);
    } else {
      return _bigDecimalFormatter.formatCell(bigDecimal, valueSpec);
    }
  }

  @Override
  public Object format(Double value, ValueSpecification valueSpec, Format format) {
    BigDecimal bigDecimal = convertToBigDecimal(value);
    if (bigDecimal == null) {
      return Double.toString(value);
    } else {
      return _bigDecimalFormatter.format(bigDecimal, valueSpec, format);
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
