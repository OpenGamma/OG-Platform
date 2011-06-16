/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.bond.calculator.CleanPriceFromCurvesCalculator;
import com.opengamma.financial.interestrate.bond.calculator.DirtyPriceFromCurvesCalculator;
import com.opengamma.financial.interestrate.bond.calculator.ModifiedDurationFromCurvesCalculator;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

/**
 * Tests related to the discounting method for bond security.
 */
public class BondSecurityDiscountingMethodTest {

  // T 4 5/8 11/15/16 - ISIN - US912828FY19
  private static final String ISSUER = "US TREASURY N/B";
  private static final String REPO_TYPE = "General collateral";
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final Period PAYMENT_TENOR_FIXED = Period.ofMonths(6);
  private static final int COUPON_PER_YEAR = 2;
  private static final DayCount DAY_COUNT_FIXED = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA");
  private static final BusinessDayConvention BUSINESS_DAY_FIXED = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final boolean IS_EOM_FIXED = false;
  private static final Period BOND_TENOR_FIXED = Period.ofYears(10);
  private static final int SETTLEMENT_DAYS = 3;
  private static final ZonedDateTime START_ACCRUAL_DATE_FIXED = DateUtil.getUTCDate(2006, 11, 15);
  private static final ZonedDateTime MATURITY_DATE_FIXED = START_ACCRUAL_DATE_FIXED.plus(BOND_TENOR_FIXED);
  private static final double RATE_FIXED = 0.04625;
  private static final double NOTIONAL = 100;
  private static final YieldConvention YIELD_CONVENTION_FIXED = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  private static final BondFixedSecurityDefinition BOND_FIXED_SECURITY_DEFINITION = BondFixedSecurityDefinition.from(CUR, MATURITY_DATE_FIXED, START_ACCRUAL_DATE_FIXED, PAYMENT_TENOR_FIXED,
      RATE_FIXED, SETTLEMENT_DAYS, NOTIONAL, CALENDAR, DAY_COUNT_FIXED, BUSINESS_DAY_FIXED, YIELD_CONVENTION_FIXED, IS_EOM_FIXED, ISSUER, REPO_TYPE);
  // To derivatives
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final String CREDIT_CURVE_NAME = "Credit";
  private static final String REPO_CURVE_NAME = "Repo";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {CREDIT_CURVE_NAME, REPO_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final YieldCurveBundle CURVES = TestsDataSets.createCurvesBond1();
  // Spot: middle coupon
  private static final ZonedDateTime REFERENCE_DATE_1 = DateUtil.getUTCDate(2011, 8, 18);
  private static final ZonedDateTime SPOT_1 = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_1, CALENDAR, SETTLEMENT_DAYS);
  private static final double REFERENCE_TIME_1 = ACT_ACT.getDayCountFraction(REFERENCE_DATE_1, SPOT_1);
  private static final BondFixedSecurity BOND_FIXED_SECURITY_1 = BOND_FIXED_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE_1, CURVES_NAME);
  // Spot: on coupon date
  private static final ZonedDateTime REFERENCE_DATE_2 = DateUtil.getUTCDate(2012, 1, 10);
  private static final ZonedDateTime SPOT_2 = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_2, CALENDAR, SETTLEMENT_DAYS);
  private static final double REFERENCE_TIME_2 = ACT_ACT.getDayCountFraction(REFERENCE_DATE_2, SPOT_2);
  private static final BondFixedSecurity BOND_FIXED_SECURITY_2 = BOND_FIXED_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE_2, CURVES_NAME);
  // Spot: one day after coupon date
  // Calculators
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final BondSecurityDiscountingMethod METHOD = new BondSecurityDiscountingMethod();

  @Test
  public void presentValueFixedMiddle() {
    final AnnuityPaymentFixed nominal = BOND_FIXED_SECURITY_DEFINITION.getNominal().toDerivative(REFERENCE_DATE_1, CURVES_NAME);
    AnnuityCouponFixed coupon = BOND_FIXED_SECURITY_DEFINITION.getCoupon().toDerivative(REFERENCE_DATE_1, CURVES_NAME);
    coupon = coupon.trimBefore(REFERENCE_TIME_1);
    final double pvNominal = PVC.visit(nominal, CURVES);
    final double pvCoupon = PVC.visit(coupon, CURVES);
    final double pv = METHOD.presentValue(BOND_FIXED_SECURITY_1, CURVES);
    assertEquals("Fixed coupon bond security: present value", pvNominal + pvCoupon, pv);
  }

  @Test
  public void presentValueFixedOnCoupon() {
    final AnnuityPaymentFixed nominal = BOND_FIXED_SECURITY_DEFINITION.getNominal().toDerivative(REFERENCE_DATE_2, CURVES_NAME);
    AnnuityCouponFixed coupon = BOND_FIXED_SECURITY_DEFINITION.getCoupon().toDerivative(REFERENCE_DATE_2, CURVES_NAME);
    coupon = coupon.trimBefore(REFERENCE_TIME_2);
    final double pvNominal = PVC.visit(nominal, CURVES);
    final double pvCoupon = PVC.visit(coupon, CURVES);
    final double pv = METHOD.presentValue(BOND_FIXED_SECURITY_2, CURVES);
    assertEquals("Fixed coupon bond security: present value", pvNominal + pvCoupon, pv);
  }

  @Test
  public void presentValueFixedMethodVsCalculator() {
    final double pvMethod = METHOD.presentValue(BOND_FIXED_SECURITY_1, CURVES);
    final double pvCalculator = PVC.visit(BOND_FIXED_SECURITY_1, CURVES);
    assertEquals("Fixed coupon bond security: present value Method vs Calculator", pvMethod, pvCalculator);
  }

  @Test
  public void dirtyPriceFixed() {
    final double pv = METHOD.presentValue(BOND_FIXED_SECURITY_1, CURVES);
    final double df = CURVES.getCurve(REPO_CURVE_NAME).getDiscountFactor(REFERENCE_TIME_1);
    final double dirty = METHOD.dirtyPriceFromCurves(BOND_FIXED_SECURITY_1, CURVES);
    assertEquals("Fixed coupon bond security: dirty price from curves", pv / df / BOND_FIXED_SECURITY_1.getCoupon().getNthPayment(0).getNotional(), dirty);
    assertTrue("Fixed coupon bond security: dirty price is relative price", (0.50 < dirty) & (dirty < 2.0));
  }

  @Test
  public void dirtyPriceFixedMethodVsCalculator() {
    final double dirtyMethod = METHOD.dirtyPriceFromCurves(BOND_FIXED_SECURITY_1, CURVES);
    final DirtyPriceFromCurvesCalculator calculator = DirtyPriceFromCurvesCalculator.getInstance();
    final double dirtyCalculator = calculator.visit(BOND_FIXED_SECURITY_1, CURVES);
    assertEquals("Fixed coupon bond security: dirty price from curves Method vs Calculator", dirtyMethod, dirtyCalculator);
  }

  @Test
  public void cleanPriceFixed() {
    final double dirty = METHOD.dirtyPriceFromCurves(BOND_FIXED_SECURITY_1, CURVES);
    final double clean = METHOD.cleanPriceFromCurves(BOND_FIXED_SECURITY_1, CURVES);
    assertEquals("Fixed coupon bond security: clean price from curves", dirty - BOND_FIXED_SECURITY_1.getAccruedInterest() / BOND_FIXED_SECURITY_1.getCoupon().getNthPayment(0).getNotional(), clean);
  }

  @Test
  public void cleanPriceFixedMethodVsCalculator() {
    final double cleanMethod = METHOD.cleanPriceFromCurves(BOND_FIXED_SECURITY_1, CURVES);
    final CleanPriceFromCurvesCalculator calculator = CleanPriceFromCurvesCalculator.getInstance();
    final double cleanCalculator = calculator.visit(BOND_FIXED_SECURITY_1, CURVES);
    assertEquals("Fixed coupon bond security: dirty price from curves Method vs Calculator", cleanMethod, cleanCalculator);
  }

  @Test
  public void cleanAndDirtyPriceFixed() {
    final double cleanPrice = 0.90;
    final double accruedInterest = BOND_FIXED_SECURITY_1.getAccruedInterest() / BOND_FIXED_SECURITY_1.getCoupon().getNthPayment(0).getNotional();
    assertEquals("Fixed coupon bond security", cleanPrice + accruedInterest, METHOD.dirtyPriceFromCleanPrice(BOND_FIXED_SECURITY_1, cleanPrice));
    final double dirtyPrice = 0.95;
    assertEquals("Fixed coupon bond security", dirtyPrice - accruedInterest, METHOD.cleanPriceFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice));
    assertEquals("Fixed coupon bond security", cleanPrice, METHOD.cleanPriceFromDirtyPrice(BOND_FIXED_SECURITY_1, METHOD.dirtyPriceFromCleanPrice(BOND_FIXED_SECURITY_1, cleanPrice)));
  }

  @Test
  public void dirtyPriceFromYieldUSStreet() {
    final double yield = 0.04;
    final double dirtyPrice = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield);
    final double dirtyPriceExpected = 1.04173525; // To be check with another source.
    assertEquals("Fixed coupon bond security: dirty price from yield", dirtyPriceExpected, dirtyPrice, 1E-8);
  }

  @Test
  public void cleanPriceFromYieldUSStreet() {
    final double yield = 0.04;
    final double dirtyPrice = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield);
    final double cleanPriceExpected = METHOD.cleanPriceFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice);
    final double cleanPrice = METHOD.cleanPriceFromYield(BOND_FIXED_SECURITY_1, yield);
    assertEquals("Fixed coupon bond security: dirty price from yield", cleanPriceExpected, cleanPrice, 1E-8);
  }

  @Test
  public void dirtyPriceFromYieldUSStreetLastPeriod() {
    final ZonedDateTime referenceDate = DateUtil.getUTCDate(2016, 6, 3); // In last period
    final BondFixedSecurity bondSecurity = BOND_FIXED_SECURITY_DEFINITION.toDerivative(referenceDate, CURVES_NAME);
    final double yield = 0.04;
    final double dirtyPrice = METHOD.dirtyPriceFromYield(bondSecurity, yield);
    final double dirtyPriceExpected = (1 + RATE_FIXED / COUPON_PER_YEAR) / (1 + bondSecurity.getAccrualFactorToNextCoupon() * yield / COUPON_PER_YEAR);
    assertEquals("Fixed coupon bond security: dirty price from yield US Street - last period", dirtyPriceExpected, dirtyPrice, 1E-8);
  }

  @Test
  public void yieldFromDirtyPriceUSStreet() {
    final double yield = 0.04;
    final double dirtyPrice = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield);
    final double yieldComputed = METHOD.yieldFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice);
    assertEquals("Fixed coupon bond security: yield from dirty price", yield, yieldComputed, 1E-10);
  }

  @Test
  public void yieldFromCurvesUSStreet() {
    final double dirtyPrice = METHOD.dirtyPriceFromCurves(BOND_FIXED_SECURITY_1, CURVES);
    final double yieldExpected = METHOD.yieldFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice);
    final double yieldComputed = METHOD.yieldFromCurves(BOND_FIXED_SECURITY_1, CURVES);
    assertEquals("Fixed coupon bond security: yield from dirty price", yieldExpected, yieldComputed, 1E-10);
  }

  @Test
  public void yieldFromCleanPriceUSStreet() {
    final double yield = 0.04;
    final double dirtyPrice = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield);
    final double cleanPrice = METHOD.cleanPriceFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice);
    final double yieldComputed = METHOD.yieldFromCleanPrice(BOND_FIXED_SECURITY_1, cleanPrice);
    assertEquals("Fixed coupon bond security: yield from clean price", yield, yieldComputed, 1E-10);
    final double cleanPrice2 = METHOD.cleanPriceFromYield(BOND_FIXED_SECURITY_1, yieldComputed);
    assertEquals("Fixed coupon bond security: yield from clean price", cleanPrice, cleanPrice2, 1E-10);
  }

  @Test
  public void modifiedDurationFromYieldUSStreet() {
    final double yield = 0.04;
    final double modifiedDuration = METHOD.modifiedDurationFromYield(BOND_FIXED_SECURITY_1, yield);
    final double modifiedDurationExpected = 4.566199225; // To be check with another source.
    assertEquals("Fixed coupon bond security: modified duration from yield US Street - hard coded value", modifiedDurationExpected, modifiedDuration, 1E-8);
    final double shift = 1.0E-6;
    final double dirty = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield);
    final double dirtyP = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield + shift);
    final double dirtyM = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield - shift);
    final double modifiedDurationFD = -(dirtyP - dirtyM) / (2 * shift) / dirty;
    assertEquals("Fixed coupon bond security: modified duration from yield US Street - finite difference", modifiedDurationFD, modifiedDuration, 1E-8);
  }

  @Test
  public void modifiedDurationFromCurvesUSStreet() {
    final double yield = METHOD.yieldFromCurves(BOND_FIXED_SECURITY_1, CURVES);
    final double modifiedDurationExpected = METHOD.modifiedDurationFromYield(BOND_FIXED_SECURITY_1, yield);
    final double modifiedDuration = METHOD.modifiedDurationFromCurves(BOND_FIXED_SECURITY_1, CURVES);
    assertEquals("Fixed coupon bond security: modified duration from curves US Street", modifiedDurationExpected, modifiedDuration, 1E-8);
  }

  @Test
  public void modifiedDurationFromDirtyPriceUSStreet() {
    final double dirtyPrice = 0.95;
    final double yield = METHOD.yieldFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice);
    final double modifiedDurationExpected = METHOD.modifiedDurationFromYield(BOND_FIXED_SECURITY_1, yield);
    final double modifiedDuration = METHOD.modifiedDurationFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice);
    assertEquals("Fixed coupon bond security: modified duration from curves US Street", modifiedDurationExpected, modifiedDuration, 1E-8);
  }

  @Test
  public void modifiedDurationFromYieldUSStreetLastPeriod() {
    final ZonedDateTime referenceDate = DateUtil.getUTCDate(2016, 6, 3); // In last period
    final BondFixedSecurity bondSecurity = BOND_FIXED_SECURITY_DEFINITION.toDerivative(referenceDate, CURVES_NAME);
    final double yield = 0.04;
    final double dirtyPrice = METHOD.modifiedDurationFromYield(bondSecurity, yield);
    final double dirtyPriceExpected = bondSecurity.getAccrualFactorToNextCoupon() / COUPON_PER_YEAR_G / (1 + bondSecurity.getAccrualFactorToNextCoupon() * yield / COUPON_PER_YEAR_G);
    assertEquals("Fixed coupon bond security: modified duration from yield US Street - last period", dirtyPriceExpected, dirtyPrice, 1E-8);
  }

  @Test
  public void modifiedDurationFromCurvesUSStreetMethodVsCalculator() {
    final double mdMethod = METHOD.modifiedDurationFromCurves(BOND_FIXED_SECURITY_1, CURVES);
    final ModifiedDurationFromCurvesCalculator calculator = ModifiedDurationFromCurvesCalculator.getInstance();
    final double mdCalculator = calculator.visit(BOND_FIXED_SECURITY_1, CURVES);
    assertEquals("Fixed coupon bond security: modified duration from curves US Street : Method vs Calculator", mdMethod, mdCalculator, 1E-8);
  }

  // UKT 5 09/07/14 - ISIN-GB0031829509 To check figures in the ex-dividend period
  private static final String ISSUER_G = "UK";
  private static final String REPO_TYPE_G = "General collateral";
  private static final Currency CUR_G = Currency.GBP;
  private static final Period PAYMENT_TENOR_G = Period.ofMonths(6);
  private static final int COUPON_PER_YEAR_G = 2;
  private static final Calendar CALENDAR_G = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_G = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"); // To check
  private static final BusinessDayConvention BUSINESS_DAY_G = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final boolean IS_EOM_G = false;
  private static final Period BOND_TENOR_G = Period.ofYears(12);
  private static final int SETTLEMENT_DAYS_G = 1;
  private static final int EX_DIVIDEND_DAYS_G = 7;
  private static final ZonedDateTime START_ACCRUAL_DATE_G = DateUtil.getUTCDate(2002, 9, 7);
  private static final ZonedDateTime MATURITY_DATE_G = START_ACCRUAL_DATE_G.plus(BOND_TENOR_G);
  private static final double RATE_G = 0.0500;
  private static final double NOTIONAL_G = 100;
  private static final YieldConvention YIELD_CONVENTION_G = YieldConventionFactory.INSTANCE.getYieldConvention("UK:BUMP/DMO METHOD");
  private static final BondFixedSecurityDefinition BOND_FIXED_SECURITY_DEFINITION_G = BondFixedSecurityDefinition.from(CUR_G, MATURITY_DATE_G, START_ACCRUAL_DATE_G, PAYMENT_TENOR_G, RATE_G,
      SETTLEMENT_DAYS_G, NOTIONAL_G, EX_DIVIDEND_DAYS_G, CALENDAR_G, DAY_COUNT_G, BUSINESS_DAY_G, YIELD_CONVENTION_G, IS_EOM_G, ISSUER_G, REPO_TYPE_G);
  private static final ZonedDateTime REFERENCE_DATE_3 = DateUtil.getUTCDate(2011, 9, 2); // Ex-dividend is 30-Aug-2011
  private static final BondFixedSecurity BOND_FIXED_SECURITY_G = BOND_FIXED_SECURITY_DEFINITION_G.toDerivative(REFERENCE_DATE_3, CURVES_NAME);
  private static final ZonedDateTime SPOT_3 = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_3, CALENDAR, SETTLEMENT_DAYS_G);
  private static final double REFERENCE_TIME_3 = ACT_ACT.getDayCountFraction(REFERENCE_DATE_3, SPOT_3);

  @Test
  public void presentValueFixedExDividend() {
    final double pv = METHOD.presentValue(BOND_FIXED_SECURITY_G, CURVES);
    final BondFixedSecurityDefinition bondNoExDefinition = BondFixedSecurityDefinition.from(CUR_G, MATURITY_DATE_G, START_ACCRUAL_DATE_G, PAYMENT_TENOR_G, RATE_G, SETTLEMENT_DAYS_G, NOTIONAL_G, 0,
        CALENDAR_G, DAY_COUNT_G, BUSINESS_DAY_G, YIELD_CONVENTION_G, IS_EOM_G, ISSUER, REPO_TYPE);
    final BondFixedSecurity BondNoEx = bondNoExDefinition.toDerivative(REFERENCE_DATE_3, CURVES_NAME);
    final double pvNoEx = METHOD.presentValue(BondNoEx, CURVES);
    final CouponFixedDefinition couponDefinitionEx = BOND_FIXED_SECURITY_DEFINITION_G.getCoupon().getNthPayment(17);
    final double pvCpn = PVC.visit(couponDefinitionEx.toDerivative(REFERENCE_DATE_3, CURVES_NAME), CURVES);
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
    assertEquals("Fixed coupon bond security: dirty price from yield UK", dirtyPriceExpected, dirtyPrice, 1E-8);
  }

  @Test
  public void dirtyPriceFromYieldUKLastPeriod() {
    final ZonedDateTime referenceDate = DateUtil.getUTCDate(2014, 6, 3); // In last period
    final BondFixedSecurity bondSecurity = BOND_FIXED_SECURITY_DEFINITION_G.toDerivative(referenceDate, CURVES_NAME);
    final double yield = 0.04;
    final double dirtyPrice = METHOD.dirtyPriceFromYield(bondSecurity, yield);
    final double dirtyPriceExpected = (1 + RATE_G / COUPON_PER_YEAR_G) * Math.pow(1 + yield / COUPON_PER_YEAR_G, -bondSecurity.getAccrualFactorToNextCoupon());
    assertEquals("Fixed coupon bond security: dirty price from yield UK - last period", dirtyPriceExpected, dirtyPrice, 1E-8);
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
    assertEquals("Fixed coupon bond security: modified duration from yield UK DMO - hard coded value", modifiedDurationExpected, modifiedDuration, 1E-8);
    final double shift = 1.0E-6;
    final double dirty = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_G, yield);
    final double dirtyP = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_G, yield + shift);
    final double dirtyM = METHOD.dirtyPriceFromYield(BOND_FIXED_SECURITY_G, yield - shift);
    final double modifiedDurationFD = -(dirtyP - dirtyM) / (2 * shift) / dirty;
    assertEquals("Fixed coupon bond security: modified duration from yield UK DMO - finite difference", modifiedDurationFD, modifiedDuration, 1E-8);
  }

  // UKT 6 1/4 11/25/10
  private static final DayCount DAY_COUNT_G2 = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"); // To check
  //  private static final Period BOND_TENOR_G2 = Period.ofYears(10);
  private static final int SETTLEMENT_DAYS_G2 = 1;
  private static final int EX_DIVIDEND_DAYS_G2 = 7;
  private static final ZonedDateTime START_ACCRUAL_DATE_G2 = DateUtil.getUTCDate(1999, 11, 25);
  private static final ZonedDateTime MATURITY_DATE_G2 = DateUtil.getUTCDate(2010, 11, 25);
  private static final double RATE_G2 = 0.0625;
  private static final double NOTIONAL_G2 = 100;
  private static final YieldConvention YIELD_CONVENTION_G2 = YieldConventionFactory.INSTANCE.getYieldConvention("UK:BUMP/DMO METHOD");
  private static final BondFixedSecurityDefinition BOND_FIXED_SECURITY_DEFINITION_G2 = BondFixedSecurityDefinition.from(CUR_G, MATURITY_DATE_G2, START_ACCRUAL_DATE_G2, PAYMENT_TENOR_G, RATE_G2,
      SETTLEMENT_DAYS_G2, NOTIONAL_G2, EX_DIVIDEND_DAYS_G2, CALENDAR_G, DAY_COUNT_G2, BUSINESS_DAY_G, YIELD_CONVENTION_G2, IS_EOM_G, ISSUER_G, REPO_TYPE_G);
  private static final ZonedDateTime REFERENCE_DATE_4 = DateUtil.getUTCDate(2001, 8, 10);
  private static final BondFixedSecurity BOND_FIXED_SECURITY_G2 = BOND_FIXED_SECURITY_DEFINITION_G2.toDerivative(REFERENCE_DATE_4, CURVES_NAME);

  @Test
  public void dirtyPriceFromCleanPriceUK() {
    final double cleanPrice = 110.20 / 100.00;
    final double dirtyPrice = METHOD.dirtyPriceFromCleanPrice(BOND_FIXED_SECURITY_G2, cleanPrice);
    final double dirtyPriceExpected = 1.11558696;
    assertEquals("Fixed coupon bond security: dirty price from clean price", dirtyPriceExpected, dirtyPrice, 1E-8);
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

}
