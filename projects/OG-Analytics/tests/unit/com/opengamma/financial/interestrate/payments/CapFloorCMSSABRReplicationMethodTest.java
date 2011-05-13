/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.financial.instrument.index.CMSIndex;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.payment.CapFloorCMSDefinition;
import com.opengamma.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameter;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

/**
 *  Test class for the replication method for CMS caplet/floorlet with a SABR smile.
 */
public class CapFloorCMSSABRReplicationMethodTest {
  //Swap 5Y
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Period ANNUITY_TENOR = Period.ofYears(5);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtil.getUTCDate(2011, 3, 17);
  //Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final AnnuityCouponFixedDefinition FIXED_ANNUITY = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, FIXED_PAYMENT_PERIOD, CALENDAR, FIXED_DAY_COUNT,
      BUSINESS_DAY, IS_EOM, 1.0, RATE, FIXED_IS_PAYER);
  //Ibor leg: quarterly money
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, 1.0, IBOR_INDEX, !FIXED_IS_PAYER);
  // CMS coupon construction
  private static final CMSIndex CMS_INDEX = new CMSIndex(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION = new SwapFixedIborDefinition(FIXED_ANNUITY, IBOR_ANNUITY);
  private static final ZonedDateTime PAYMENT_DATE = DateUtil.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime FIXING_DATE = DateUtil.getUTCDate(2010, 12, 30);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtil.getUTCDate(2011, 1, 5);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtil.getUTCDate(2011, 4, 5);
  private static final DayCount PAYMENT_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final double ACCRUAL_FACTOR = PAYMENT_DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; //1m
  private static final CouponCMSDefinition CMS_COUPON_DEFINITION = CouponCMSDefinition.from(PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION,
      CMS_INDEX);
  // Cap/Floor construction
  private static final double STRIKE = 0.04;
  private static final boolean IS_CAP = true;
  private static final CapFloorCMSDefinition CMS_CAP_DEFINITION = CapFloorCMSDefinition.from(CMS_COUPON_DEFINITION, STRIKE, IS_CAP);
  private static final CapFloorCMSDefinition CMS_CAP_0_DEFINITION = CapFloorCMSDefinition.from(CMS_COUPON_DEFINITION, 0.0, IS_CAP);
  private static final CapFloorCMSDefinition CMS_FLOOR_DEFINITION = CapFloorCMSDefinition.from(CMS_COUPON_DEFINITION, STRIKE, !IS_CAP);
  private static final CouponFixedDefinition COUPON_STRIKE_DEFINITION = new CouponFixedDefinition(CMS_COUPON_DEFINITION, STRIKE);
  // to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtil.getUTCDate(2010, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};

  private static final CouponCMS CMS_COUPON = (CouponCMS) CMS_COUPON_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CapFloorCMS CMS_CAP_0 = (CapFloorCMS) CMS_CAP_0_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CapFloorCMS CMS_CAP = (CapFloorCMS) CMS_CAP_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CapFloorCMS CMS_FLOOR = (CapFloorCMS) CMS_FLOOR_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CouponFixed COUPON_STRIKE = COUPON_STRIKE_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Calculators
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();

  @Test
  /**
   * Tests the price of CMS coupon and cap/floor using replication in the SABR framework. Values are tested against hard-coded values.
   */
  public void testPriceReplication() {
    final YieldCurveBundle curves = TestsDataSets.createCurves1();
    final SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    // CMS cap/floor with strike 0 has the same price as a CMS coupon.
    final double priceCMSCoupon = PVC.visit(CMS_COUPON, sabrBundle);
    final double priceCMSCap0 = PVC.visit(CMS_CAP_0, sabrBundle);
    assertEquals(priceCMSCoupon, priceCMSCap0, 1E-2);
    final double priceCMSCap = PVC.visit(CMS_CAP, sabrBundle);
    assertEquals(717.182, priceCMSCap, 1E-2);//From previous run
    final double priceCMSFloor = PVC.visit(CMS_FLOOR, sabrBundle);
    assertEquals(597.902, priceCMSFloor, 1E-2);//From previous run
    final double priceStrike = PVC.visit(COUPON_STRIKE, curves);
    // Cap/floor parity: !cash-settled swaption price is arbitrable: no exact cap/floor/swap parity!
    assertEquals(priceCMSCap - priceCMSFloor + 24.0, priceCMSCoupon - priceStrike, 1.0);
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void testPerformance() {
    final YieldCurveBundle curves = TestsDataSets.createCurves1();
    final SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    final CouponCMSSABRReplicationMethod replication = new CouponCMSSABRReplicationMethod();
    long startTime, endTime;
    final int nbTest = 1000;
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      replication.presentValue(CMS_CAP, sabrBundle);
      replication.presentValueSensitivity(CMS_CAP, sabrBundle);
      replication.presentValueSABRSensitivity(CMS_CAP, sabrBundle);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS cap by replication (price+delta+vega): " + (endTime - startTime) + " ms");
    // Performance note: price+delta: 04-May: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 415 ms for 1000 cap 5Y.
    // Performance note: price+delta+vega: 04-May: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 820 ms for 1000 cap 5Y.
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      replication.presentValue(CMS_FLOOR, sabrBundle);
      replication.presentValueSensitivity(CMS_FLOOR, sabrBundle);
      replication.presentValueSABRSensitivity(CMS_FLOOR, sabrBundle);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS floor by replication (price+delta+vega): " + (endTime - startTime) + " ms");
    // Performance note: price+delta: 04-May-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 410 ms for 1000 floor 5Y.
    // Performance note: price+delta+vega: 04-May-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 810 ms for 1000 cap 5Y.
  }
}
