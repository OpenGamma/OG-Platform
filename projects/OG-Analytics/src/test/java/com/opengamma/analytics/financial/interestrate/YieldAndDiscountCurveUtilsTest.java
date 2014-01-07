/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.MulticurveCalibratedUSDDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Tests for the Utils function related to YieldAndDiscount curves and MulticurveProvider.
 */
@Test(groups = TestGroup.UNIT)
public class YieldAndDiscountCurveUtilsTest {

  /** Calibrated curves **/
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> CURVE_PAIR = MulticurveCalibratedUSDDataSets.getCurvesUSD();
  private static final IborIndex USDLIBOR6M = IndexIborMaster.getInstance().getIndex("USDLIBOR6M");
  private static final IborIndex USDLIBOR3M = IndexIborMaster.getInstance().getIndex("USDLIBOR3M");
  private static final MulticurveProviderDiscount MULTICURVE = CURVE_PAIR.getFirst();
  private static final Calendar NYC = new MondayToFridayCalendar("NYC");

  private static final ZonedDateTime CURVE_DATE = DateUtils.getUTCDate(2013, 6, 19);

  private static final double TOLERANCE_RATE = 1E-8;

  @Test
  public void forwardRateCurveTest() {
    final YieldAndDiscountCurve curve3M = MULTICURVE.getCurve(USDLIBOR3M);
    final ZonedDateTime fixingDate = DateUtils.getUTCDate(2013, 10, 28);
    final ZonedDateTime fixingPeriodStartDate = ScheduleCalculator.getAdjustedDate(fixingDate, USDLIBOR6M.getSpotLag(), NYC);
    final double fixingPeriodStartTime = TimeCalculator.getTimeBetween(CURVE_DATE, fixingPeriodStartDate);
    final ZonedDateTime fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(fixingDate, USDLIBOR6M, NYC);
    final double fixingPeriodEndTime = TimeCalculator.getTimeBetween(CURVE_DATE, fixingPeriodEndDate);
    final double accrualFixing = USDLIBOR6M.getDayCount().getDayCountFraction(fixingPeriodStartDate, fixingPeriodEndDate);
    final double dfStart = curve3M.getDiscountFactor(fixingPeriodStartTime);
    final double dfEnd = curve3M.getDiscountFactor(fixingPeriodEndTime);
    final double forwardExpected = (dfStart / dfEnd - 1.0d) / accrualFixing;
    final double forwardComputedCurve = YieldAndDiscountCurveUtils.forwardRateFromCurve(curve3M, CURVE_DATE, fixingDate, USDLIBOR6M, NYC);
    assertEquals("forwardRateCurve", forwardExpected, forwardComputedCurve, TOLERANCE_RATE);
  }

  @Test
  public void forwardRateProviderTest() {
    final YieldAndDiscountCurve curve3M = MULTICURVE.getCurve(USDLIBOR3M);
    final ZonedDateTime fixingDate = DateUtils.getUTCDate(2013, 10, 28);
    final ZonedDateTime fixingPeriodStartDate = ScheduleCalculator.getAdjustedDate(fixingDate, USDLIBOR3M.getSpotLag(), NYC);
    final double fixingPeriodStartTime = TimeCalculator.getTimeBetween(CURVE_DATE, fixingPeriodStartDate);
    final ZonedDateTime fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(fixingDate, USDLIBOR3M, NYC);
    final double fixingPeriodEndTime = TimeCalculator.getTimeBetween(CURVE_DATE, fixingPeriodEndDate);
    final double accrualFixing = USDLIBOR3M.getDayCount().getDayCountFraction(fixingPeriodStartDate, fixingPeriodEndDate);
    final double dfStart = curve3M.getDiscountFactor(fixingPeriodStartTime);
    final double dfEnd = curve3M.getDiscountFactor(fixingPeriodEndTime);
    final double forwardExpected = (dfStart / dfEnd - 1.0d) / accrualFixing;
    final double forwardComputedProvider = YieldAndDiscountCurveUtils.forwardRateFromProvider(MULTICURVE, CURVE_DATE, fixingDate, USDLIBOR3M, NYC);
    assertEquals("forwardRateProvider", forwardExpected, forwardComputedProvider, TOLERANCE_RATE);
  }

  @Test
  public void zeroCouponRatePeriodicTest() {
    final YieldAndDiscountCurve curve3M = MULTICURVE.getCurve(USDLIBOR3M);
    final ZonedDateTime paymentDate = DateUtils.getUTCDate(2013, 10, 28);
    final double timeCurve = TimeCalculator.getTimeBetween(CURVE_DATE, paymentDate);
    final double df = curve3M.getDiscountFactor(timeCurve);
    final DayCount dc = DayCounts.ACT_365;
    final double timeDc = dc.getDayCountFraction(CURVE_DATE, paymentDate);
    final int paymentPerYear = 4;
    final double rateExpected = paymentPerYear * (Math.pow(df, -1.0 / (paymentPerYear * timeDc)) - 1.0);
    final double rateComputed = YieldAndDiscountCurveUtils.zeroCouponRate(curve3M, CURVE_DATE, paymentDate, dc, paymentPerYear);
    assertEquals("zeroCouponRatePeriodic", rateExpected, rateComputed, TOLERANCE_RATE);
  }

  @Test
  public void zeroCouponRateContinuousTest() {
    final YieldAndDiscountCurve curve3M = MULTICURVE.getCurve(USDLIBOR3M);
    final ZonedDateTime paymentDate = DateUtils.getUTCDate(2013, 10, 28);
    final double timeCurve = TimeCalculator.getTimeBetween(CURVE_DATE, paymentDate);
    final double df = curve3M.getDiscountFactor(timeCurve);
    final DayCount dc = DayCounts.ACT_365;
    final double timeDc = dc.getDayCountFraction(CURVE_DATE, paymentDate);
    final double rateExpected = -Math.log(df) / timeDc;
    final double rateComputed = YieldAndDiscountCurveUtils.zeroCouponRate(curve3M, CURVE_DATE, paymentDate, dc, 0);
    assertEquals("zeroCouponRatePeriodic", rateExpected, rateComputed, TOLERANCE_RATE);
  }

}
