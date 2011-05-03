/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;
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
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameter;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

/**
 *  Test class for the replication method for CMS caplet/floorlet using a SABR smile with extrapolation.
 */
public class CapFloorSABRExtrapolationRightReplicationMethodTest {
  //Swap 5Y
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Period ANNUITY_TENOR = Period.ofYears(5);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtil.getUTCDate(2020, 4, 28);
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
  private static final ZonedDateTime FIXING_DATE = ScheduleCalculator.getAdjustedDate(SETTLEMENT_DATE, CALENDAR, -SETTLEMENT_DAYS);
  private static final ZonedDateTime ACCRUAL_START_DATE = SETTLEMENT_DATE; // pre-fixed
  private static final ZonedDateTime ACCRUAL_END_DATE = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATE, BUSINESS_DAY, CALENDAR, FIXED_PAYMENT_PERIOD);
  private static final ZonedDateTime PAYMENT_DATE = ACCRUAL_END_DATE;
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
  //  private static final CapFloorCMSDefinition CMS_FLOOR_DEFINITION = CapFloorCMSDefinition.from(CMS_COUPON_DEFINITION, STRIKE, !IS_CAP);
  //  private static final CouponFixedDefinition COUPON_STRIKE_DEFINITION = new CouponFixedDefinition(CMS_COUPON_DEFINITION, STRIKE);
  // to derivatives
  private static final LocalDate REFERENCE_DATE = LocalDate.of(2010, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};

  private static final CouponCMS CMS_COUPON = (CouponCMS) CMS_COUPON_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CapFloorCMS CMS_CAP_0 = (CapFloorCMS) CMS_CAP_0_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final CapFloorCMS CMS_CAP = (CapFloorCMS) CMS_CAP_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  //  private static final CapFloorCMS CMS_FLOOR = (CapFloorCMS) CMS_FLOOR_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  //  private static final CouponFixed COUPON_STRIKE = COUPON_STRIKE_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Calculators & methods
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final CapFloorCMSReplicationSABRMethod METHOD_STANDARD_CAP = new CapFloorCMSReplicationSABRMethod();
  private static final CouponCMSReplicationSABRMethod METHOD_STANDARD_CPN = new CouponCMSReplicationSABRMethod();
  private static final double CUT_OFF_STRIKE = 0.10;
  private static final double MU = 2.50;
  private static final CapFloorCMSSABRExtrapolationRightReplicationMethod METHOD_EXTRAPOLATION_CAP = new CapFloorCMSSABRExtrapolationRightReplicationMethod(CUT_OFF_STRIKE, MU);
  private static final CouponCMSSABRExtrapolationRightReplicationMethod METHOD_EXTRAPOLATION_CPN = new CouponCMSSABRExtrapolationRightReplicationMethod(CUT_OFF_STRIKE, MU);

  @Test
  public void testPriceReplicationCoupon() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    // CMS cap/floor with strike 0 has the same price as a CMS coupon.
    double priceCouponStd = METHOD_STANDARD_CPN.presentValue(CMS_COUPON, sabrBundle);
    double rateCouponStd = priceCouponStd / (CMS_COUPON.getPaymentYearFraction() * CMS_COUPON.getNotional() * curves.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(CMS_COUPON.getPaymentTime()));
    double priceCouponExtra = METHOD_EXTRAPOLATION_CPN.presentValue(CMS_COUPON, sabrBundle);
    double rateCouponExtra = priceCouponExtra / (CMS_COUPON.getPaymentYearFraction() * CMS_COUPON.getNotional() * curves.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(CMS_COUPON.getPaymentTime()));
    double priceCouponNoAdj = PVC.visit(CMS_COUPON, curves);
    double rateCouponNoAdj = priceCouponNoAdj / (CMS_COUPON.getPaymentYearFraction() * CMS_COUPON.getNotional() * curves.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(CMS_COUPON.getPaymentTime()));
    assertEquals("Extrapolation: comparison with standard method", rateCouponStd > rateCouponExtra, true);
    assertEquals("Extrapolation: comparison with no convexity adjustment", rateCouponExtra > rateCouponNoAdj, true);
    double rateCouponExtraExpected = 0.0485823; // From previous run.
    assertEquals("Extrapolation: hard-coded value", rateCouponExtraExpected, rateCouponExtra, 1E-6);
    double priceCap0Extra = METHOD_EXTRAPOLATION_CAP.presentValue(CMS_CAP_0, sabrBundle);
    assertEquals("Extrapolation: CMS coupon vs Cap 0", priceCouponExtra, priceCap0Extra, 1E-2);
  }

  @Test
  public void testPriceReplicationCap() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    // CMS cap/floor with strike 0 has the same price as a CMS coupon.
    double priceCapStd = METHOD_STANDARD_CAP.presentValue(CMS_CAP, sabrBundle);
    double priceCapExtra = METHOD_EXTRAPOLATION_CAP.presentValue(CMS_CAP, sabrBundle);
    assertEquals("Extrapolation: comparison with standard method", priceCapStd > priceCapExtra, true);
    double priceCapExtraExpected = 6627.855; // From previous run.
    assertEquals("Extrapolation: hard-coded value", priceCapExtraExpected, priceCapExtra, 1E-2);
  }

  @Test(enabled = false)
  public void testPriceMultiMu() {
    // To estimate the impact of mu on CMS pricing
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    double[] mu = new double[] {1.10, 1.30, 1.55, 2.25, 3.50, 6.00, 15.0};
    int nbMu = mu.length;
    double priceCouponStd = METHOD_STANDARD_CPN.presentValue(CMS_COUPON, sabrBundle);
    double rateCouponStd = priceCouponStd / (CMS_COUPON.getPaymentYearFraction() * CMS_COUPON.getNotional() * curves.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(CMS_COUPON.getPaymentTime()));
    double[] priceCouponExtra = new double[nbMu];
    double[] rateCouponExtra = new double[nbMu];
    for (int loopmu = 0; loopmu < nbMu; loopmu++) {
      CouponCMSSABRExtrapolationRightReplicationMethod methodExtrapolation = new CouponCMSSABRExtrapolationRightReplicationMethod(CUT_OFF_STRIKE, mu[loopmu]);
      priceCouponExtra[loopmu] = methodExtrapolation.presentValue(CMS_COUPON, sabrBundle);
      rateCouponExtra[loopmu] = priceCouponExtra[loopmu]
          / (CMS_COUPON.getPaymentYearFraction() * CMS_COUPON.getNotional() * curves.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(CMS_COUPON.getPaymentTime()));
    }
    double priceCouponNoAdj = PVC.visit(CMS_COUPON, curves);
    double rateCouponNoAdj = priceCouponNoAdj / (CMS_COUPON.getPaymentYearFraction() * CMS_COUPON.getNotional() * curves.getCurve(FUNDING_CURVE_NAME).getDiscountFactor(CMS_COUPON.getPaymentTime()));
    assertEquals("Extrapolation: comparison with standard method", rateCouponStd > rateCouponExtra[0], true);
    for (int loopmu = 1; loopmu < nbMu; loopmu++) {
      assertEquals("Extrapolation: comparison with standard method", rateCouponExtra[loopmu - 1] > rateCouponExtra[loopmu], true);
    }
    assertEquals("Extrapolation: comparison with standard method", rateCouponExtra[nbMu - 1] > rateCouponNoAdj, true);
  }

  @Test(enabled = false)
  public void testPerformance() {
    // Used only to assess performance
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    long startTime, endTime;
    int nbTest = 1000;
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      METHOD_STANDARD_CPN.presentValue(CMS_COUPON, sabrBundle);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS cap by replication SABR standard (price): " + (endTime - startTime) + " ms");
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      METHOD_EXTRAPOLATION_CPN.presentValue(CMS_COUPON, sabrBundle);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS cap by replication SABR with extrapolation (price): " + (endTime - startTime) + " ms");
    // Performance note: price (standard SABR): 27-Apr-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 70 ms for 1000 cap 5Y.
    // Performance note: price (SABR with extrapolation): 27-Apr-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 132 ms for 1000 cap 5Y.
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      METHOD_STANDARD_CPN.presentValue(CMS_COUPON, sabrBundle);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS cap by replication SABR standard (price): " + (endTime - startTime) + " ms");
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      METHOD_EXTRAPOLATION_CPN.presentValue(CMS_COUPON, sabrBundle);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " CMS cap by replication SABR with extrapolation (price): " + (endTime - startTime) + " ms");
  }

}
