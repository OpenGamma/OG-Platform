/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import java.math.BigDecimal;

/**
 * Utility class to do basic money calculation
 */
public final class MoneyCalculationUtil {
  
  /**
   * The number of decimals to retain.
   */
  public static final int DECIMALS = 2;
  
  /**
   * The rounding mode.
   */
  public static final int ROUNDING_MODE = BigDecimal.ROUND_HALF_EVEN;

  private MoneyCalculationUtil() {
    
  }
  
  public static BigDecimal add(final BigDecimal amountOne, final BigDecimal amountTwo) {
    return rounded(amountOne).add(rounded(amountTwo));
  }
  
  public static BigDecimal minus(final BigDecimal amountOne, final BigDecimal amountTwo) {
    return rounded(amountOne).subtract(rounded(amountTwo));
  }
  
  public static  BigDecimal rounded(BigDecimal number) {
    return number.setScale(DECIMALS, ROUNDING_MODE);
  }
}
