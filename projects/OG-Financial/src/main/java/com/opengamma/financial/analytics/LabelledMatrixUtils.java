/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import org.threeten.bp.Period;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public final class LabelledMatrixUtils {

  protected static final Period TENOR_TOLERANCE = Period.ofDays(1);
  
  /**
   * Hidden constructor.
   */
  private LabelledMatrixUtils() {
  }
  
  protected static <T> Object[] toString(final T[] arr) {
    ArgumentChecker.notNull(arr, "arr");
    final Object[] result = new Object[arr.length];
    for (int i = 0; i < arr.length; i++) {
      result[i] = arr[i].toString();
    }
    return result;
  }
  
  protected static int compareTenorsWithTolerance(Tenor d1, Tenor d2, Period tolerance) {
    if (tolerance.equals(TENOR_TOLERANCE)) {
      return d1.compareTo(d2); //TOLERANCE == 1ns => this degenerate case
    }
    if (d1.equals(d2)) {
      return 0;
    }
    final Period dLow = d1.getPeriod().minus(tolerance);
    final Period deltaLow = d2.getPeriod().minus(dLow);
    if (deltaLow.isNegative()) {
      return -1;
    }
    final Period dHigh = d1.getPeriod().plus(tolerance);
    final Period deltaHigh = dHigh.minus(d2.getPeriod());
    if (deltaHigh.isNegative()) {
      return 1;
    }
    return 0;
  }
  
}
