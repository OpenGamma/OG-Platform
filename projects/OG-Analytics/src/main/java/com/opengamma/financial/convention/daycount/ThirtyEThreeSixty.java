/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import org.threeten.bp.LocalDate;

/**
 * The '30E/360' day count.
 */
public class ThirtyEThreeSixty extends ThirtyThreeSixtyTypeDayCount {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  @Override
  public double getDayCountFraction(final LocalDate firstDate, final LocalDate secondDate) {
    testDates(firstDate, secondDate);
    double d1 = firstDate.getDayOfMonth();
    double d2 = secondDate.getDayOfMonth();
    final double m1 = firstDate.getMonthValue();
    final double m2 = secondDate.getMonthValue();
    final double y1 = firstDate.getYear();
    final double y2 = secondDate.getYear();
    if (d1 == 31) {
      d1 = 30;
    }
    if (d2 == 31) {
      d2 = 30;
    }
    return getYears(d1, d2, m1, m2, y1, y2);
  }

  @Override
  public String getName() {
    return "30E/360";
  }

}
