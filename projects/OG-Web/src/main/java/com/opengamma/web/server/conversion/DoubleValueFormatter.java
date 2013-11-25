/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;

import com.opengamma.util.ArgumentChecker;

/**
 * Abstract base class for formatting double values.
 */
public abstract class DoubleValueFormatter {
  
  private static final int GROUP_SIZE = 3;
  private static final char PLAIN_STRING_MINUS_SIGN = '-';
  private static final char PLAIN_STRING_DECIMAL_SEPARATOR = '.';
  
  private final char _localeDecimalSeparator;
  private final char _localeGroupingSeparator;
  private final char _localeMinusSign;
  private final boolean _plainStringMatchesLocale;
  
  private final boolean _isCurrencyAmount;
  
  public DoubleValueFormatter(boolean isCurrencyAmount) {
    this(isCurrencyAmount, DecimalFormatSymbols.getInstance());
  }
  
  public DoubleValueFormatter(boolean isCurrencyAmount, DecimalFormatSymbols formatSymbols) {
    _localeGroupingSeparator = formatSymbols.getGroupingSeparator();
    _localeDecimalSeparator = formatSymbols.getDecimalSeparator();
    _localeMinusSign = formatSymbols.getMinusSign();
    _plainStringMatchesLocale = _localeMinusSign == PLAIN_STRING_MINUS_SIGN && _localeDecimalSeparator == PLAIN_STRING_DECIMAL_SEPARATOR;
    
    _isCurrencyAmount = isCurrencyAmount;
  }
  
  public boolean isCurrencyAmount() {
    return _isCurrencyAmount;
  }
  
  /**
   * Transforms a {@link BigDecimal} value as required, for example setting the scale and
   * precision.
   * 
   * @param value  the input value, not null
   * @return the processed value, not null
   */
  protected abstract BigDecimal process(BigDecimal value);
  
  public BigDecimal getRoundedValue(BigDecimal value) {
    return process(value);
  }
  
  public String format(BigDecimal value) {
    BigDecimal processedValue = process(value);
    return transformPlainNumberString(processedValue.toPlainString());
  }
  
  /**
   * Takes a plain number (rounded appropriately, and using '.' as the decimal separator and '-' if negative) and
   * applies locale-specific formatting.
   * 
   * @param plainNumberString  the plain number, not null
   * @return the transformed number, not null
   */
  /* package */ String transformPlainNumberString(String plainNumberString) {
    // A plain number string is of the form:
    //   (-)?[0-9]+(\.[0-9]+)?
    // i.e. using '-' for negative numbers and '.' as the decimal separator.
    ArgumentChecker.notNull(plainNumberString, "plainNumberString");
    int decimalIdx = plainNumberString.indexOf(PLAIN_STRING_DECIMAL_SEPARATOR);
    boolean isNegative = plainNumberString.charAt(0) == PLAIN_STRING_MINUS_SIGN;
    
    int integerStartIdx = isNegative ? 1 : 0;
    int integerEndIdx = decimalIdx > -1 ? decimalIdx : plainNumberString.length();
    String integerPart = plainNumberString.substring(integerStartIdx, integerEndIdx);
    int integerPartLength = integerPart.length();
    
    int firstGroupEndIdx = integerPartLength % GROUP_SIZE;
    if (firstGroupEndIdx == 0) {
      firstGroupEndIdx = GROUP_SIZE;
    }
    if (firstGroupEndIdx == integerPartLength && _plainStringMatchesLocale) {
      // Nothing to group and plain string matches locale formatting
      return plainNumberString;
    }
    StringBuilder sb = new StringBuilder(plainNumberString.length() + integerPartLength / 3);
    if (isNegative) {
      sb.append(_localeMinusSign);
    }
    sb.append(integerPart.substring(0, firstGroupEndIdx));
    for (int i = firstGroupEndIdx; i < integerPartLength; i += GROUP_SIZE) {
      sb.append(_localeGroupingSeparator).append(integerPart.substring(i, i + 3));
    }
    if (decimalIdx > -1) {
      sb.append(_localeDecimalSeparator).append(plainNumberString.substring(decimalIdx + 1));        
    }
    return sb.toString();
  }
  
}
