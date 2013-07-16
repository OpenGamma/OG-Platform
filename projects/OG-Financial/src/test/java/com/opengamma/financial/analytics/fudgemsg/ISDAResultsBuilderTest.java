/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class ISDAResultsBuilderTest extends AnalyticsTestBase {

  @Test
  public void testISDADateCurve() {
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 1, 1);
    final ZonedDateTime[] dates = new ZonedDateTime[] { now, now.plusYears(1) };
    final ISDADateCurve c1 = new ISDADateCurve("Test", dates, new double[] { 0, 1 }, new double[] { 1, 2 }, 0);
    final ISDADateCurve c2 = cycleObject(ISDADateCurve.class, c1);
    assertEquals(c1, c2);
  }

  @Test
  public void testHazardRateCurve() {
    final ZonedDateTime now = DateUtils.getUTCDate(2013, 1, 1);
    final ZonedDateTime[] dates = new ZonedDateTime[] {now, now.plusMonths(3), now.plusMonths(6), now.plusMonths(9), now.plusMonths(12)};
    final double[] times = new double[] {0, .25, .5, .75, 1};
    final double[] rates = new double[] {0.01, 0.02, 0.03, 0.04, 0.05};
    final double offset = 1. / 360;
    final HazardRateCurve curve = new HazardRateCurve(dates, times, rates, offset);
    assertEquals(curve, cycleObject(HazardRateCurve.class, curve));
  }

  @Test
  public void testISDACompliantCurve() {
    final double[] times = new double[] {0, .25, .5, .75, 1};
    final double[] rates = new double[] {0.01, 0.02, 0.03, 0.04, 0.05};
    final ISDACompliantCurve curve = new ISDACompliantCurve(times, rates);
    assertEquals(curve, cycleObject(ISDACompliantCurve.class, curve));
  }

  @Test
  public void testISDACompliantCreditCurve() {
    final double[] times = new double[] {0, .25, .5, .75, 1};
    final double[] rates = new double[] {0.01, 0.02, 0.03, 0.04, 0.05};
    final ISDACompliantCreditCurve curve = new ISDACompliantCreditCurve(times, rates);
    assertEquals(curve, cycleObject(ISDACompliantCreditCurve.class, curve));
  }

  @Test
  public void testISDACompliantYieldCurve() {
    final double[] times = new double[] {0, .25, .5, .75, 1};
    final double[] rates = new double[] {0.01, 0.02, 0.03, 0.04, 0.05};
    final ISDACompliantYieldCurve curve = new ISDACompliantYieldCurve(times, rates);
    assertEquals(curve, cycleObject(ISDACompliantYieldCurve.class, curve));
  }

}
