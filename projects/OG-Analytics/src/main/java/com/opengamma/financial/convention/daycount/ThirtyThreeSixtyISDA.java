/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import org.threeten.bp.LocalDate;

/**
 * The '30/360 ISDA' day count.
 */
public class ThirtyThreeSixtyISDA extends ThirtyThreeSixtyTypeDayCount {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  @Override
  public double getDayCountFraction(LocalDate firstDate, LocalDate secondDate) {
    testDates(firstDate, secondDate);
    double d1 = firstDate.getDayOfMonth();
    double d2 = secondDate.getDayOfMonth();
    double m1 = firstDate.getMonthValue();
    double m2 = secondDate.getMonthValue();
    double y1 = firstDate.getYear();
    double y2 = secondDate.getYear();
    if (d1 == 31) {
      d1 = 30;
    }
    if (d2 == 31 && d1 == 30) {
      d2 = 30;
    }
    return getYears(d1, d2, m1, m2, y1, y2);
  }

  @Override
  public String getName() {
    return "30/360 ISDA";
  }

}
