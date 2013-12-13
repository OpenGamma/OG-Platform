/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.bond.calculator.CleanPriceFromCurvesCalculator;
import com.opengamma.analytics.financial.interestrate.bond.calculator.CleanPriceFromYieldCalculator;
import com.opengamma.analytics.financial.interestrate.bond.calculator.ConvexityFromCurvesCalculator;
import com.opengamma.analytics.financial.interestrate.bond.calculator.DirtyPriceFromCurvesCalculator;
import com.opengamma.analytics.financial.interestrate.bond.calculator.DirtyPriceFromYieldCalculator;
import com.opengamma.analytics.financial.interestrate.bond.calculator.MacaulayDurationFromCurvesCalculator;
import com.opengamma.analytics.financial.interestrate.bond.calculator.MacaulayDurationFromYieldCalculator;
import com.opengamma.analytics.financial.interestrate.bond.calculator.ModifiedDurationFromCleanPriceCalculator;
import com.opengamma.analytics.financial.interestrate.bond.calculator.ModifiedDurationFromCurvesCalculator;
import com.opengamma.analytics.financial.interestrate.bond.calculator.ModifiedDurationFromYieldCalculator;
import com.opengamma.analytics.financial.interestrate.bond.calculator.YieldFromCurvesCalculator;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the discounting method for bond security.
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class BondSecurityUKDiscountingMethodTest {

  // Calculators
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final BondSecurityDiscountingMethod METHOD = BondSecurityDiscountingMethod.getInstance();

  private static final double TOLERANCE_PRICE = 1.0E-8;

  private static final Calendar LON = new MondayToFridayCalendar("A");

  // To derivatives
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final String CREDIT_CURVE_NAME = "Credit";
  private static final String REPO_CURVE_NAME = "Repo";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {CREDIT_CURVE_NAME, REPO_CURVE_NAME, FORWARD_CURVE_NAME };
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurvesBond1();

  // UKT 5 09/07/14 - ISIN-GB0031829509 To check figures in the ex-dividend period
  private static final String ISSUER_G = "UK";
  private static final String REPO_TYPE_G = "General collateral";
  private static final Currency CUR_G = Currency.GBP;
  private static final Period PAYMENT_TENOR_G = Period.ofMonths(6);
  private static final int COUPON_PER_YEAR_G = 2;
  private static final Calendar CALENDAR_G = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_G = DayCounts.ACT_ACT_ICMA; // To check
  private static final BusinessDayConvention BUSINESS_DAY_G = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM_G = false;
  private static final Period BOND_TENOR_G = Period.ofYears(12);
  private static final int SETTLEMENT_DAYS_G = 1;
  private static final int EX_DIVIDEND_DAYS_G = 7;
  private static final ZonedDateTime START_ACCRUAL_DATE_G = DateUtils.getUTCDate(2002, 9, 7);
  private static final ZonedDateTime MATURITY_DATE_G = START_ACCRUAL_DATE_G.plus(BOND_TENOR_G);
  private static final double RATE_G = 0.0500;
  private static final double NOTIONAL_G = 100;
  private static final YieldConvention YIELD_CONVENTION_G = YieldConventionFactory.INSTANCE.getYieldConvention("UK:BUMP/DMO METHOD");
  private static final BondFixedSecurityDefinition BOND_FIXED_SECURITY_DEFINITION_G = BondFixedSecurityDefinition.from(CUR_G, MATURITY_DATE_G, START_ACCRUAL_DATE_G, PAYMENT_TENOR_G, RATE_G,
      SETTLEMENT_DAYS_G, NOTIONAL_G, EX_DIVIDEND_DAYS_G, CALENDAR_G, DAY_COUNT_G, BUSINESS_DAY_G, YIELD_CONVENTION_G, IS_EOM_G, ISSUER_G, REPO_TYPE_G);
  private static final ZonedDateTime REFERENCE_DATE_3 = DateUtils.getUTCDate(2011, 9, 2); // Ex-dividend is 30-Aug-2011
  private static final BondFixedSecurity BOND_FIXED_SECURITY_G = BOND_FIXED_SECURITY_DEFINITION_G.toDerivative(REFERENCE_DATE_3, CURVES_NAME);
  private static final ZonedDateTime SPOT_3 = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_3, SETTLEMENT_DAYS_G, LON);
  private static final double REFERENCE_TIME_3 = ACT_ACT.getDayCountFraction(REFERENCE_DATE_3, SPOT_3);
  private final static YieldFromCurvesCalculator YFCC = YieldFromCurvesCalculator.getInstance();
  private final static ModifiedDurationFromCurvesCalculator MDFC = ModifiedDurationFromCurvesCalculator.getInstance();
  private static final ModifiedDurationFromYieldCalculator MDFY = ModifiedDurationFromYieldCalculator.getInstance();
  private static final ModifiedDurationFromCleanPriceCalculator MDFP = ModifiedDurationFromCleanPriceCalculator.getInstance();
  private static final MacaulayDurationFromCurvesCalculator McDFC = MacaulayDurationFromCurvesCalculator.getInstance();
  private static final MacaulayDurationFromYieldCalculator McDFY = MacaulayDurationFromYieldCalculator.getInstance();
  private static final DirtyPriceFromYieldCalculator DPFY = DirtyPriceFromYieldCalculator.getInstance();
  private static final DirtyPriceFromCurvesCalculator DPFC = DirtyPriceFromCurvesCalculator.getInstance();
  private static final ConvexityFromCurvesCalculator CFC = ConvexityFromCurvesCalculator.getInstance();
  private static final CleanPriceFromYieldCalculator CPFY = CleanPriceFromYieldCalculator.getInstance();
  private static final CleanPriceFromCurvesCalculator CPFC = CleanPriceFromCurvesCalculator.getInstance();

  @Test
  public void presentValueFixedExDividend() {
    final double pv = METHOD.presentValue(BOND_FIXED_SECURITY_G, CURVES);
    final BondFixedSecurityDefinition bondNoExDefinition = BondFixedSecurityDefinition.from(CUR_G, MATURITY_DATE_G, START_ACCRUAL_DATE_G, PAYMENT_TENOR_G, RATE_G, SETTLEMENT_DAYS_G, NOTIONAL_G, 0,
        CALENDAR_G, DAY_COUNT_G, BUSINESS_DAY_G, YIELD_CONVENTION_G, IS_EOM_G, ISSUER_G, REPO_TYPE_G);
    final BondFixedSecurity BondNoEx = bondNoExDefinition.toDerivative(REFERENCE_DATE_3, CURVES_NAME);
    final double pvNoEx = METHOD.presentValue(BondNoEx, CURVES);
    final CouponFixedDefinition couponDefinitionEx = BOND_FIXED_SECURITY_DEFINITION_G.getCoupons().getNthPayment(17);
    final double pvCpn = couponDefinitionEx.toDerivative(REFERENCE_DATE_3, CURVES_NAME).accept(PVC, CURVES);
    assertEquals("Fixed coupon bond security: present value ex dividend", pvNoEx - pvCpn, pv, 1.0E-6);
  }

  @Test
  public void dirtyPriceFixedExDividend() {
    final double pv = METHOD.presentValue(BOND_FIXED_SECURITY_G, CURVES);
    final double df = CURVES.getCurve(REPO_CURVE_NAME).getDiscountFactor(REFERENCE_TIME_3);
    final double dirty = METHOD.dirtyPriceFromCurves(BOND_FIXED_SECURITY_G, CURVES);
    assertEquals("Fixed coupon bond security: dirty price from curves", pv / df / BOND_FIXED_SECURITY_G.getCoupon().getNthPayment(0).getNotional(), dirty);
    assertTrue("Fixed coupon bond security: dirty price is relative price", (0.50 < dirty) & (dirty < 2.0));
  }

  @Test
  public void dirtyPriceFromYieldUKExDividend() {
    final double yield = 0.04;
    final double dirtyPrice = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_G, yield);
    final double dirtyPriceExpected = 1.0277859038; // To be check with another source.
    assertEquals("Fixed coupon bond security: dirty price from yield UK", dirtyPriceExpected, dirtyPrice, TOLERANCE_PRICE);
  }

  @Test
  public void dirtyPriceFromYieldUKLastPeriod() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2014, 6, 3); // In last period
    final BondFixedSecurity bondSecurity = BOND_FIXED_SECURITY_DEFINITION_G.toDerivative(referenceDate, CURVES_NAME);
    final double yield = 0.04;
    final double dirtyPrice = METHOD.dirtyPriceFromYield(bondSecurity, yield);
    final double dirtyPriceExpected = (1 + RATE_G / COUPON_PER_YEAR_G) * Math.pow(1 + yield / COUPON_PER_YEAR_G, -bondSecurity.getFactorToNextCoupon());
    assertEquals("Fixed coupon bond security: dirty price from yield UK - last period", dirtyPriceExpected, dirtyPrice, TOLERANCE_PRICE);
  }

  @Test
  public void yieldFromDirtyPriceUKExDividend() {
    final double yield = 0.04;
    final double dirtyPrice = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_G, yield);
    final double yieldComputed = METHOD.yieldFromDirtyPrice(BOND_FIXED_SECURITY_G, dirtyPrice);
    assertEquals("Fixed coupon bond security: yield from dirty price UK", yield, yieldComputed, 1E-10);
  }

  @Test
  public void modifiedDurationFromYieldUKExDividend() {
    final double yield = 0.04;
    final double modifiedDuration = METHOD.modifiedDurationFromYield(BOND_FIXED_SECURITY_G, yield);
    final double modifiedDurationExpected = 2.7757118292; // To be check with another source.
    assertEquals("Fixed coupon bond security: modified duration from yield UK DMO - hard coded value", modifiedDurationExpected, modifiedDuration, TOLERANCE_PRICE);
    final double shift = 1.0E-6;
    final double dirty = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_G, yield);
    final double dirtyP = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_G, yield + shift);
    final double dirtyM = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_G, yield - shift);
    final double modifiedDurationFD = -(dirtyP - dirtyM) / (2 * shift) / dirty;
    assertEquals("Fixed coupon bond security: modified duration from yield UK DMO - finite difference", modifiedDurationFD, modifiedDuration, TOLERANCE_PRICE);
  }

  @Test
  public void macauleyDurationFromYieldUKExDividend() {
    final double yield = 0.04;
    final double macauleyDuration = METHOD.macaulayDurationFromYield(BOND_FIXED_SECURITY_G, yield);
    final double macauleyDurationExpected = 2.909894241 / METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_G, yield); // To be check with another source.
    assertEquals("Fixed coupon bond security: Macauley duration from yield UK DMO - hard coded value", macauleyDurationExpected, macauleyDuration, TOLERANCE_PRICE);
  }

  // UKT 6 1/4 11/25/10
  private static final DayCount DAY_COUNT_G2 = DayCounts.ACT_ACT_ICMA; // To check
  //  private static final Period BOND_TENOR_G2 = Period.ofYears(10);
  private static final int SETTLEMENT_DAYS_G2 = 1;
  private static final int EX_DIVIDEND_DAYS_G2 = 7;
  private static final ZonedDateTime START_ACCRUAL_DATE_G2 = DateUtils.getUTCDate(1999, 11, 25);
  private static final ZonedDateTime MATURITY_DATE_G2 = DateUtils.getUTCDate(2010, 11, 25);
  private static final double RATE_G2 = 0.0625;
  private static final double NOTIONAL_G2 = 100;
  private static final YieldConvention YIELD_CONVENTION_G2 = YieldConventionFactory.INSTANCE.getYieldConvention("UK:BUMP/DMO METHOD");
  private static final BondFixedSecurityDefinition BOND_FIXED_SECURITY_DEFINITION_G2 = BondFixedSecurityDefinition.from(CUR_G, MATURITY_DATE_G2, START_ACCRUAL_DATE_G2, PAYMENT_TENOR_G, RATE_G2,
      SETTLEMENT_DAYS_G2, NOTIONAL_G2, EX_DIVIDEND_DAYS_G2, CALENDAR_G, DAY_COUNT_G2, BUSINESS_DAY_G, YIELD_CONVENTION_G2, IS_EOM_G, ISSUER_G, REPO_TYPE_G);
  private static final ZonedDateTime REFERENCE_DATE_4 = DateUtils.getUTCDate(2001, 8, 10);
  private static final BondFixedSecurity BOND_FIXED_SECURITY_G2 = BOND_FIXED_SECURITY_DEFINITION_G2.toDerivative(REFERENCE_DATE_4, CURVES_NAME);

  @Test
  public void dirtyPriceFromCleanPriceUK() {
    final double cleanPrice = 110.20 / 100.00;
    final double dirtyPrice = METHOD.dirtyPriceFromCleanPrice(BOND_FIXED_SECURITY_G2, cleanPrice);
    final double dirtyPriceExpected = 1.11558696;
    assertEquals("Fixed coupon bond security: dirty price from clean price", dirtyPriceExpected, dirtyPrice, TOLERANCE_PRICE);
  }

  @Test
  public void yieldPriceFromCleanPriceUK() {
    final double cleanPrice = 110.20 / 100.00;
    final double yield = METHOD.yieldFromCleanPrice(BOND_FIXED_SECURITY_G2, cleanPrice);
    final double yieldExpected = 0.04870;
    assertEquals("Fixed coupon bond security: dirty price from clean price", yieldExpected, yield, 1E-5);
  }

  @Test
  public void modifiedDurationFromCleanPriceUK() {
    final double cleanPrice = 110.20 / 100.00;
    final double dirtyPrice = METHOD.dirtyPriceFromCleanPrice(BOND_FIXED_SECURITY_G2, cleanPrice);
    final double md = METHOD.modifiedDurationFromDirtyPrice(BOND_FIXED_SECURITY_G2, dirtyPrice);
    final double mdExpected = 7.039;
    assertEquals("Fixed coupon bond security: dirty price from clean price", mdExpected, md, 1E-3);
  }

  @Test
  public void yieldFromCurvesMethodVsCalculator() {
    final double yieldMethod = METHOD.yieldFromCurves(BOND_FIXED_SECURITY_G2, CURVES);
    final double yieldCalculator = BOND_FIXED_SECURITY_G2.accept(YFCC, CURVES);
    assertEquals("bond Security: discounting method - yield", yieldMethod, yieldCalculator, 1e-9);
  }

  @Test
  public void modifiedDurationMethodVsCalculator() {
    double method = METHOD.modifiedDurationFromCurves(BOND_FIXED_SECURITY_G2, CURVES);
    double calculator = BOND_FIXED_SECURITY_G2.accept(MDFC, CURVES);
    assertEquals("bond Security: discounting method - modified duration", method, calculator, 1e-9);
    method = METHOD.modifiedDurationFromYield(BOND_FIXED_SECURITY_G2, 0.05);
    calculator = BOND_FIXED_SECURITY_G2.accept(MDFY, 0.05);
    assertEquals("bond Security: discounting method - modified duration", method, calculator, 1e-9);
    method = METHOD.modifiedDurationFromCleanPrice(BOND_FIXED_SECURITY_G2, 1.00);
    calculator = BOND_FIXED_SECURITY_G2.accept(MDFP, 1.00);
    assertEquals("bond Security: discounting method - modified duration", method, calculator, 1e-9);
  }

  @Test
  public void macaulayDurationMethodVsCalculator() {
    double method = METHOD.macaulayDurationFromCurves(BOND_FIXED_SECURITY_G2, CURVES);
    double calculator = BOND_FIXED_SECURITY_G2.accept(McDFC, CURVES);
    assertEquals("bond Security: discounting method - macaulay duration", method, calculator, 1e-9);
    method = METHOD.macaulayDurationFromYield(BOND_FIXED_SECURITY_G2, 0.05);
    calculator = BOND_FIXED_SECURITY_G2.accept(McDFY, 0.05);
    assertEquals("bond Security: discounting method - macaulay duration", method, calculator, 1e-9);
  }

  @Test
  public void dirtyPriceMethodVsCalculator() {
    double method = METHOD.dirtyPriceFromCurves(BOND_FIXED_SECURITY_G2, CURVES);
    double calculator = BOND_FIXED_SECURITY_G2.accept(DPFC, CURVES);
    assertEquals("bond Security: discounting method - dirty price", method, calculator, 1e-9);
    method = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_G2, 0.05);
    calculator = BOND_FIXED_SECURITY_G2.accept(DPFY, 0.05);
    assertEquals("bond Security: discounting method - dirty price", method, calculator, 1e-9);
  }

  @Test
  public void convexityMethodVsCalculator() {
    final double method = METHOD.convexityFromCurves(BOND_FIXED_SECURITY_G2, CURVES);
    final double calculator = BOND_FIXED_SECURITY_G2.accept(CFC, CURVES);
    assertEquals("bond Security: discounting method - convexity", method, calculator, 1e-9);
  }

  @Test
  public void cleanPriceMethodVsCalculator() {
    double method = METHOD.cleanPriceFromCurves(BOND_FIXED_SECURITY_G2, CURVES);
    double calculator = BOND_FIXED_SECURITY_G2.accept(CPFC, CURVES);
    assertEquals("bond Security: discounting method - clean price", method, calculator, 1e-9);
    method = METHOD.cleanPriceFromYield(BOND_FIXED_SECURITY_G2, 0.05);
    calculator = BOND_FIXED_SECURITY_G2.accept(CPFY, 0.05);
    assertEquals("bond Security: discounting method - clean price", method, calculator / 100, 1e-9);
  }
}
