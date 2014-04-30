/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class PriceIndexCurveTest {

  private static double[] INDEX_VALUE = new double[] {108.23, 108.64, 111.0, 115.0};
  private static double[] TIME_VALUE = new double[] {-3.0 / 12.0, -2.0 / 12.0, 9.0 / 12.0, 2.0 + 9.0 / 12.0};
  private static final InterpolatedDoublesCurve CURVE = InterpolatedDoublesCurve.from(TIME_VALUE, INDEX_VALUE, new LinearInterpolator1D());
  private static final PriceIndexCurve PRICE_INDEX_CURVE = new PriceIndexCurve(CURVE);

  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurve() {
    new PriceIndexCurve(null);
  }

  @Test
  /**
   * Tests the getter.
   */
  public void getter() {
    assertEquals(CURVE, PRICE_INDEX_CURVE.getCurve());
  }

  @Test
  /**
   * Tests price index.
   */
  public void priceIndex() {
    assertEquals(INDEX_VALUE[0], PRICE_INDEX_CURVE.getPriceIndex(TIME_VALUE[0]), 1.0E-10);
    assertEquals(INDEX_VALUE[2], PRICE_INDEX_CURVE.getPriceIndex(TIME_VALUE[2]), 1.0E-10);
    assertEquals((INDEX_VALUE[2] + INDEX_VALUE[3]) / 2.0, PRICE_INDEX_CURVE.getPriceIndex((TIME_VALUE[2] + TIME_VALUE[3]) / 2.0), 1.0E-10);
  }

  @Test
  /**
   * Tests price index builder from zero-coupon swap rates with start of the month convention.
   */
  public void fromStartOfMonth() {
    ZonedDateTime constructionDate = DateUtils.getUTCDate(2011, 8, 18);
    ZonedDateTime[] indexKnownDate = new ZonedDateTime[] {DateUtils.getUTCDate(2011, 5, 1), DateUtils.getUTCDate(2011, 6, 1)};
    double[] nodeTimeKnown = new double[indexKnownDate.length];
    for (int loopmonth = 0; loopmonth < indexKnownDate.length; loopmonth++) {
      nodeTimeKnown[loopmonth] = -ACT_ACT.getDayCountFraction(indexKnownDate[loopmonth], constructionDate);
    }
    int[] swapTenor = new int[] {1, 2, 3, 4, 5, 7, 10, 15, 20, 30};
    double[] swapRate = new double[] {0.02, 0.021, 0.02, 0.025, 0.025, 0.025, 0.025, 0.025, 0.025, 0.025};
    double[] indexKnown = new double[] {113.11, 113.10}; // May / June 2011.
    int monthLag = 3;
    double[] nodeTimeOther = new double[swapTenor.length];
    ZonedDateTime[] referenceDate = new ZonedDateTime[swapTenor.length];
    for (int loopswap = 0; loopswap < swapTenor.length; loopswap++) {
      ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(constructionDate, Period.ofYears(swapTenor[loopswap]), BUSINESS_DAY, CALENDAR);
      referenceDate[loopswap] = paymentDate.minusMonths(monthLag).withDayOfMonth(1);
      nodeTimeOther[loopswap] = ACT_ACT.getDayCountFraction(constructionDate, referenceDate[loopswap]);
    }
    PriceIndexCurve priceIndexCurve = PriceIndexCurve.fromStartOfMonth(nodeTimeKnown, indexKnown, nodeTimeOther, swapRate);
    for (int loopswap = 0; loopswap < swapTenor.length; loopswap++) {
      assertEquals("Simple price curve", indexKnown[0] * Math.pow(1 + swapRate[loopswap], swapTenor[loopswap]), priceIndexCurve.getPriceIndex(nodeTimeOther[loopswap]));
    }
  }
}
