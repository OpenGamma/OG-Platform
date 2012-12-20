/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 
 */
public class DoubleValueSizeBasedDecimalPlaceFormatter extends DoubleValueFormatter {

  // CSOFF
  public static final DoubleValueSizeBasedDecimalPlaceFormatter CCY_DEFAULT = new DoubleValueSizeBasedDecimalPlaceFormatter(2, 0, 10, true);
  // CSON
  
  private final int _smallNumberDecimalPlaces;
  private final int _largeNumberDecimalPlaces;
  private final int _threshold;
  
  public DoubleValueSizeBasedDecimalPlaceFormatter(int smallNumberDecimalPlaces, int largeNumberDecimalPlaces, int threshold, boolean isCurrencyAmount) {
    super(isCurrencyAmount);
    _smallNumberDecimalPlaces = smallNumberDecimalPlaces;
    _largeNumberDecimalPlaces = largeNumberDecimalPlaces;
    _threshold = threshold;
  }
  
  @Override
  protected BigDecimal process(BigDecimal value) {
    int scale = Math.abs(value.longValue()) > _threshold ? _largeNumberDecimalPlaces : _smallNumberDecimalPlaces;
    return value.setScale(scale, RoundingMode.HALF_UP);
  }

}
