/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormatSymbols;

/**
 * 
 */
public class DoubleValueSignificantFiguresFormatter extends DoubleValueFormatter {
  
  // CSOFF
  public static final DoubleValueSignificantFiguresFormatter NON_CCY_5SF = DoubleValueSignificantFiguresFormatter.of(5, false);
  // CSON
  
  private final BigDecimal _maxValueForSigFig;
  private final MathContext _sigFigMathContext;
  
  public DoubleValueSignificantFiguresFormatter(int significantFigures, boolean isCurrencyAmount) {
    this(significantFigures, isCurrencyAmount, DecimalFormatSymbols.getInstance());
  }
  
  public DoubleValueSignificantFiguresFormatter(int significantFigures, boolean isCurrencyAmount, DecimalFormatSymbols formatSymbols) {
    super(isCurrencyAmount, formatSymbols);
    _maxValueForSigFig = BigDecimal.TEN.pow(significantFigures - 1);
    _sigFigMathContext = new MathContext(significantFigures, RoundingMode.HALF_UP);
  }
  
  public static DoubleValueSignificantFiguresFormatter of(int significantFigures, boolean isCurrencyAmount) {
    return new DoubleValueSignificantFiguresFormatter(significantFigures, isCurrencyAmount);
  }
  
  @Override
  public BigDecimal process(BigDecimal bigDecimalValue) {
    if (bigDecimalValue.abs().compareTo(_maxValueForSigFig) > 0) {
      return bigDecimalValue.setScale(0, RoundingMode.HALF_UP);
    } else {
      return bigDecimalValue.round(_sigFigMathContext);
    }
  }
  
}
