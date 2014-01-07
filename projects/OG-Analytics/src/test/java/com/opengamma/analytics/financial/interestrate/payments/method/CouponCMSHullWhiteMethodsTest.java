/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.method;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.model.interestrate.TestsDataSetHullWhite;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing of CMS coupons with Hull-White (extended Vasicek) model and different numerical methods.
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class CouponCMSHullWhiteMethodsTest {

  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor GENERATOR_EUR1YEURIBOR6M = GENERATOR_SWAP_MASTER.getGenerator("EUR1YEURIBOR6M", TARGET);
  private static final Period TENOR_SWAP = Period.ofYears(10);
  private static final IndexSwap SWAP_EUR10Y = new IndexSwap(GENERATOR_EUR1YEURIBOR6M, TENOR_SWAP);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 1, 17);

  // Coupon CMS: 6m fixing in advance (payment in arrears); ACT/360
  private static final Period TENOR_COUPON = Period.ofMonths(6);
  private static final Period TENOR_FIXING = Period.ofMonths(60);
  private static final DayCount ACT360 = DayCounts.ACT_360;
  private static final ZonedDateTime FIXING_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, TENOR_FIXING, GENERATOR_EUR1YEURIBOR6M.getBusinessDayConvention(), TARGET,
      GENERATOR_EUR1YEURIBOR6M.isEndOfMonth());
  private static final ZonedDateTime START_DATE = ScheduleCalculator.getAdjustedDate(FIXING_DATE, GENERATOR_EUR1YEURIBOR6M.getSpotLag(), TARGET);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, TENOR_COUPON, GENERATOR_EUR1YEURIBOR6M.getBusinessDayConvention(), TARGET,
      GENERATOR_EUR1YEURIBOR6M.isEndOfMonth());
  private static final double NOTIONAL = 100000000; //100m
  private static final double ACCRUAL_FACTOR = ACT360.getDayCountFraction(START_DATE, PAYMENT_DATE);
  private static final CouponCMSDefinition CPN_CMS_DEFINITION = CouponCMSDefinition.from(PAYMENT_DATE, START_DATE, PAYMENT_DATE, ACCRUAL_FACTOR, NOTIONAL, SWAP_EUR10Y, TARGET);

  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves2();
  private static final String[] CURVE_NAMES = TestsDataSetsSABR.curves2Names();
  private static final HullWhiteOneFactorPiecewiseConstantParameters PARAMETERS_HW = TestsDataSetHullWhite.createHullWhiteParameters();
  private static final HullWhiteOneFactorPiecewiseConstantDataBundle BUNDLE_HW = new HullWhiteOneFactorPiecewiseConstantDataBundle(PARAMETERS_HW, CURVES);

  private static final CouponCMS CPN_CMS = (CouponCMS) CPN_CMS_DEFINITION.toDerivative(REFERENCE_DATE, new String[] {CURVE_NAMES[0], CURVE_NAMES[2]});

  private static final CouponCMSHullWhiteNumericalIntegrationMethod METHOD_NI = CouponCMSHullWhiteNumericalIntegrationMethod.getInstance();
  private static final CouponCMSHullWhiteApproximationMethod METHOD_APP = CouponCMSHullWhiteApproximationMethod.getInstance();
  private static final CouponCMSDiscountingMethod METHOD_DSC = CouponCMSDiscountingMethod.getInstance();
  private static final double TOLERANCE_PRICE = 1.0E-2;
  private static final double TOLERANCE_PRICE_APP = 5.0E+0;

  @Test
  public void presentValueNumericalIntegration() {
    final CurrencyAmount pvNumericalIntegration = METHOD_NI.presentValue(CPN_CMS, BUNDLE_HW);
    final double pvPrevious = 1124760.482; // From previous run
    assertEquals("Coupon CMS - Hull-White - present value - numerical integration", pvPrevious, pvNumericalIntegration.getAmount(), TOLERANCE_PRICE);
    // Comparison with non-adjusted figures: to have the right order of magnitude
    final CurrencyAmount pvDiscounting = METHOD_DSC.presentValue(CPN_CMS, BUNDLE_HW);
    assertEquals("Coupon CMS - Hull-White - present value - numerical integration", 1.0, pvDiscounting.getAmount() / pvNumericalIntegration.getAmount(), 0.20);
  }

  @Test
  public void presentValueApproximation() {
    final CurrencyAmount pvNumericalIntegration = METHOD_NI.presentValue(CPN_CMS, BUNDLE_HW);
    final CurrencyAmount pvApproximation = METHOD_APP.presentValue(CPN_CMS, BUNDLE_HW);
    assertEquals("Coupon CMS - Hull-White - present value - approximation", pvApproximation.getAmount(), pvNumericalIntegration.getAmount(), TOLERANCE_PRICE_APP);
  }

}
