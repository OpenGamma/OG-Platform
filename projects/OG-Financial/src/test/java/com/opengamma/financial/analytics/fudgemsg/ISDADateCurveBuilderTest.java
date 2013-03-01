/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;

/**
 *
 */
public class ISDADateCurveBuilderTest extends AnalyticsTestBase {

  @SuppressWarnings("unchecked")
  @Test
  public void testNodalDoubleCurve() {
    ZonedDateTime now = ZonedDateTime.now();
    ZonedDateTime[] dates = new ZonedDateTime[] { now, now.plusYears(1) };
    ISDADateCurve c1 = new ISDADateCurve("Test", dates, new double[] { 0, 1 }, new double[] { 1, 2 }, 0);
    ISDADateCurve c2 = cycleObject(ISDADateCurve.class, c1);
  }
}
