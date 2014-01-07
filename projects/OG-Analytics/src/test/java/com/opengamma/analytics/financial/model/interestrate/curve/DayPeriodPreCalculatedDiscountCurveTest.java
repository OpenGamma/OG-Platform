/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.analytics.financial.model.interestrate.curve;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DayPeriodPreCalculatedDiscountCurveTest {

  private static double TOLERANCE = 1e-10;
  // note the time periods passed in must be a whole number divided by 365.25, otherwise the pre calculated factors
  // will not line up
  private static double[] x = new double[] {0 / DateUtils.DAYS_PER_YEAR, 1 / DateUtils.DAYS_PER_YEAR,
      2 / DateUtils.DAYS_PER_YEAR, 100 / DateUtils.DAYS_PER_YEAR, 293 / DateUtils.DAYS_PER_YEAR,
      309 / DateUtils.DAYS_PER_YEAR, 428 / DateUtils.DAYS_PER_YEAR, 567 / DateUtils.DAYS_PER_YEAR,
      5634 / DateUtils.DAYS_PER_YEAR};
  private static double[] y = new double[] {1.0, 0.75, 0.5, 0.25, 0.15, 0.12, 0.10, 0.9, 0.85};
  private static InterpolatedDoublesCurve DOUBLES_CURVE = InterpolatedDoublesCurve.from(x, y, Interpolator1DFactory.LINEAR_INSTANCE);
  private static final DiscountCurve EXISTING_CURVE = DiscountCurve.from(DOUBLES_CURVE);

  @Test(groups = TestGroup.UNIT)
  public void testGetDiscountFactor() throws Exception {
    DayPeriodPreCalculatedDiscountCurve curve = new DayPeriodPreCalculatedDiscountCurve("test", DOUBLES_CURVE, DateUtils.DAYS_PER_YEAR);
    curve.preCalculateDiscountFactors(15);
    for (int i = 0; i < x[x.length - 1]; i++) {
      double t = i / DateUtils.DAYS_PER_YEAR;
      Assert.assertEquals(EXISTING_CURVE.getDiscountFactor(t), curve.getDiscountFactor(t), TOLERANCE);
    }
  }
}
