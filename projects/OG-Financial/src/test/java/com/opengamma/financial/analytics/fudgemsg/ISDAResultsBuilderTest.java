/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.util.test.TestGroup;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class ISDAResultsBuilderTest extends AnalyticsTestBase {

  @Test
  public void testISDACompliantCurve() {
    final double[] times = new double[] {0, .25, .5, .75, 1 };
    final double[] rates = new double[] {0.01, 0.02, 0.03, 0.04, 0.05 };
    final ISDACompliantCurve curve = new ISDACompliantCurve(times, rates);
    assertEquals(curve, cycleObject(ISDACompliantCurve.class, curve));
  }

  @Test
  public void testISDACompliantCreditCurve() {
    final double[] times = new double[] {0, .25, .5, .75, 1 };
    final double[] rates = new double[] {0.01, 0.02, 0.03, 0.04, 0.05 };
    final ISDACompliantCreditCurve curve = new ISDACompliantCreditCurve(times, rates);
    assertEquals(curve, cycleObject(ISDACompliantCreditCurve.class, curve));
  }

  @Test
  public void testISDACompliantYieldCurve() {
    final double[] times = new double[] {0, .25, .5, .75, 1 };
    final double[] rates = new double[] {0.01, 0.02, 0.03, 0.04, 0.05 };
    final ISDACompliantYieldCurve curve = new ISDACompliantYieldCurve(times, rates);
    assertEquals(curve, cycleObject(ISDACompliantYieldCurve.class, curve));
  }

}
