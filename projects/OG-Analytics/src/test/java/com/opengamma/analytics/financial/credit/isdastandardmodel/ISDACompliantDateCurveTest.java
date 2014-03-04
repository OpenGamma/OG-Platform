/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

/**
 * 
 */
public class ISDACompliantDateCurveTest {

  @Test
  public void test() {

    final LocalDate baseDate = LocalDate.of(2013, 2, 3);
    final LocalDate[] dates = new LocalDate[] {LocalDate.of(2013, 5, 14), LocalDate.of(2013, 9, 13), LocalDate.of(2013, 9, 14), LocalDate.of(2014, 1, 23) };
    final double[] rates = new double[] {0.05, 0.06, 0.06, 0.04 };
    final int num = dates.length;

    final ISDACompliantDateCurve curve = new ISDACompliantDateCurve(baseDate, dates, rates);
    for (int i = 0; i < num; ++i) {
      curve.getCurveDate(i);
    }
    //    curve.getCurveDates();
    //    curve.withRate(rates[num - 2] * 2., num - 2);
    //
    //    curve.meta();
    //    curve.metaBean();
  }
}
