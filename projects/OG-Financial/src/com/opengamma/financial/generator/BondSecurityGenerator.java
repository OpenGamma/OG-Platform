/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.generator;

import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatterBuilder;

import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.util.time.Expiry;

/**
 * Source of random, but reasonable, bond security instances.
 * 
 * @param <T> the subtype of the bond generated
 */
public abstract class BondSecurityGenerator<T extends BondSecurity> extends SecurityGenerator<T> {

  /**
   * Coupon rate constants.
   */
  private static final String[] COUPON_RATES = new String[] {"", " 1/8", " 1/4", " 3/8", " 1/2", " 5/8", " 3/4", " 7/8" };

  /**
   * Formatter for the date used to construct a name.
   */
  private static final DateTimeFormatter NAME_DATE_FORMATTER = new DateTimeFormatterBuilder().appendPattern("MM/dd/yy").toFormatter();

  /**
   * Bond length constants (years).
   */
  private static final int[] LENGTHS = new int[] {1, 2, 5, 10, 15, 20, 30 };

  protected String createName(final String prefix, final int couponRateEighths, final Expiry expiry) {
    return prefix + " " + (couponRateEighths / 8) + COUPON_RATES[couponRateEighths % 8] + " " + expiry.getExpiry().toString(NAME_DATE_FORMATTER);
  }

  protected int getRandomLength() {
    return getRandom(LENGTHS);
  }

}
