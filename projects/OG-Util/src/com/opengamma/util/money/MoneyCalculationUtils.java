/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utilities that handle basic money calculation.
 * <p>
 * This is a thread-safe static utility class.
 */
public final class MoneyCalculationUtils {

  /**
   * The number of decimals to retain.
   */
  public static final int DECIMALS = 2;
  /**
   * The rounding mode.
   */
  public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

  /**
   * Restrictive constructor.
   */
  private MoneyCalculationUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Adds two amounts rounding to two decimal places.
   * 
   * @param baseAmount  the base amount, not null
   * @param amountToAdd  the amount to add, not null
   * @return the total, not null
   */
  public static BigDecimal add(final BigDecimal baseAmount, final BigDecimal amountToAdd) {
    return rounded(baseAmount).add(rounded(amountToAdd));
  }

  /**
   * Subtract one amount from another rounding to two decimal places.
   * 
   * @param baseAmount  the amount to subtract from, not null
   * @param amountToSubtract  the amount to subtract, not null
   * @return the subtraction result, not null
   */
  public static BigDecimal subtract(final BigDecimal baseAmount, final BigDecimal amountToSubtract) {
    return rounded(baseAmount).subtract(rounded(amountToSubtract));
  }

  /**
   * Rounds an amount to two decimal places.
   * 
   * @param amount  the amount to round, not null
   * @return the rounded amount, not null
   */
  public static BigDecimal rounded(BigDecimal amount) {
    return amount.setScale(DECIMALS, ROUNDING_MODE);
  }

}
