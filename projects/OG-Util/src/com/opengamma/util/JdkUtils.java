/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.math.BigDecimal;

/**
 * Utility to fix bugs in the JDK.
 */
public final class JdkUtils {

  /**
   * Restricted constructor.
   */
  private JdkUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Strips trailing zeros from a BigDecimal.
   * <p>
   * The JDK does not strip zeros from zero.
   * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6480539">Bug 6480539</a>.
   * 
   * @param decimal  the big decimal to strip, not null
   * @return the stripped decimal, not null
   */
  public static BigDecimal stripTrailingZeros(final BigDecimal decimal) {
    BigDecimal zero = BigDecimal.valueOf(0, 0);
    if (decimal.compareTo(zero) == 0) {
      return zero;
    } else {
      return decimal.stripTrailingZeros();
    }
  }

}
