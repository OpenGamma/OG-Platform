/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import com.opengamma.engine.value.ValueSpecification;

/**
 * General converter for doubles that applies rounding rules.
 */
public class DoubleConverter implements ResultConverter<Object> {

  private final int _defaultSigFig = 5;
  
  @Override
  public Object convert(ResultConverterCache context, ValueSpecification valueSpec, Object value, ConversionMode mode) {
    double doubleValue = (Double) value;
    long maxValueForSigFig = (long) Math.pow(10, _defaultSigFig - 1);
    if (doubleValue > maxValueForSigFig) {
      return Long.toString(Math.round(doubleValue));
    } else {
      MathContext mathContext = new MathContext(5, RoundingMode.HALF_UP);
      return new BigDecimal(doubleValue, mathContext).toString();
    }
  }

  @Override
  public String getFormatterName() {
    return "PRIMITIVE";
  }

}
