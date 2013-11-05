/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.newcommodity.curve;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;

/**
 * 
 */
public class CommodityForwardCurveTest {

  private static double[] FWD_VALUE = new double[] {108.23, 108.64, 111.0, 115.0 };
  private static double[] TIME_VALUE = new double[] {-3.0 / 12.0, -2.0 / 12.0, 9.0 / 12.0, 2.0 + 9.0 / 12.0 };
  private static final InterpolatedDoublesCurve CURVE = InterpolatedDoublesCurve.from(TIME_VALUE, FWD_VALUE, new LinearInterpolator1D());
  private static final CommodityForwardCurve COMMODITY_FORWARD_CURVE = new CommodityForwardCurve(CURVE);

  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurve() {
    new CommodityForwardCurve(null);
  }

  @Test
  /**
   * Tests the getter.
   */
  public void getter() {
    assertEquals(CURVE, COMMODITY_FORWARD_CURVE.getFwdCurve());
  }

  @Test
  /**
   * Tests price index.
   */
  public void priceIndex() {
    assertEquals(FWD_VALUE[0], COMMODITY_FORWARD_CURVE.getForwardValue(TIME_VALUE[0]), 1.0E-10);
    assertEquals(FWD_VALUE[2], COMMODITY_FORWARD_CURVE.getForwardValue(TIME_VALUE[2]), 1.0E-10);
    assertEquals((FWD_VALUE[2] + FWD_VALUE[3]) / 2.0, COMMODITY_FORWARD_CURVE.getForwardValue((TIME_VALUE[2] + TIME_VALUE[3]) / 2.0), 1.0E-10);
  }

}
