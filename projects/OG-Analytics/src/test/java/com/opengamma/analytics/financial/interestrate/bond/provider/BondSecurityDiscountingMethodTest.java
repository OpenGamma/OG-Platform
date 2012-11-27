/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.IssuerProviderIssuerDecoratedSpread;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountingDecoratedIssuer;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the discounting method for bond security.
 */
public class BondSecurityDiscountingMethodTest {

  private final static IssuerProviderDiscount ISSUER_MULTICURVES = IssuerProviderDiscountDataSets.createIssuerProvider();
  private final static String[] ISSUER_NAMES = IssuerProviderDiscountDataSets.getIssuerNames();
  public static final String NOT_USED = "Not used";
  public static final String[] NOT_USED_A = {NOT_USED, NOT_USED, NOT_USED};
  // T 4 5/8 11/15/16 - ISIN - US912828FY19
  private static final String ISSUER = ISSUER_NAMES[0];
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final Period PAYMENT_TENOR_FIXED = Period.ofMonths(6);
  private static final int COUPON_PER_YEAR = 2;
  private static final DayCount DAY_COUNT_FIXED = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA");
  private static final BusinessDayConvention BUSINESS_DAY_FIXED = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final boolean IS_EOM_FIXED = false;
  private static final Period BOND_TENOR_FIXED = Period.ofYears(10);
  private static final int SETTLEMENT_DAYS = 3;
  private static final ZonedDateTime START_ACCRUAL_DATE_FIXED = DateUtils.getUTCDate(2006, 11, 15);
  private static final ZonedDateTime MATURITY_DATE_FIXED = START_ACCRUAL_DATE_FIXED.plus(BOND_TENOR_FIXED);
  private static final double RATE_FIXED = 0.04625;
  private static final double NOTIONAL = 100;
  private static final YieldConvention YIELD_CONVENTION_FIXED = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  private static final BondFixedSecurityDefinition BOND_FIXED_SECURITY_DEFINITION = BondFixedSecurityDefinition.from(CUR, MATURITY_DATE_FIXED, START_ACCRUAL_DATE_FIXED, PAYMENT_TENOR_FIXED,
      RATE_FIXED, SETTLEMENT_DAYS, NOTIONAL, CALENDAR, DAY_COUNT_FIXED, BUSINESS_DAY_FIXED, YIELD_CONVENTION_FIXED, IS_EOM_FIXED, ISSUER, NOT_USED);
  // To derivatives

  // Spot: middle coupon
  private static final ZonedDateTime REFERENCE_DATE_1 = DateUtils.getUTCDate(2011, 8, 18);
  private static final ZonedDateTime SPOT_1 = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_1, SETTLEMENT_DAYS, CALENDAR);
  private static final double REFERENCE_TIME_1 = TimeCalculator.getTimeBetween(REFERENCE_DATE_1, SPOT_1);
  private static final BondFixedSecurity BOND_FIXED_SECURITY_1 = BOND_FIXED_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE_1, NOT_USED_A);
  // Spot: on coupon date
  private static final ZonedDateTime REFERENCE_DATE_2 = DateUtils.getUTCDate(2012, 1, 10);
  private static final ZonedDateTime SPOT_2 = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_2, SETTLEMENT_DAYS, CALENDAR);
  private static final double REFERENCE_TIME_2 = TimeCalculator.getTimeBetween(REFERENCE_DATE_2, SPOT_2);
  private static final BondFixedSecurity BOND_FIXED_SECURITY_2 = BOND_FIXED_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE_2, NOT_USED_A);
  // Spot: one day after coupon date
  // Calculators
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueIssuerCalculator PVIC = PresentValueIssuerCalculator.getInstance();
  private static final BondSecurityDiscountingMethod METHOD_BOND_SECURITY = BondSecurityDiscountingMethod.getInstance();

  private static final double TOLERANCE_PV = 1.0E-8;
  private static final double TOLERANCE_PV_DELTA = 1.0E-4;
  private static final double TOLERANCE_PRICE = 1.0E-8;
  private static final double TOLERANCE_YIELD = 1.0E-8;

  //  private static final double TOLERANCE_YIELD_DERIVATIVE = 1.0E-6;

  @Test
  public void presentValueFixedMiddle() {
    final AnnuityPaymentFixed nominal = (AnnuityPaymentFixed) BOND_FIXED_SECURITY_DEFINITION.getNominal().toDerivative(REFERENCE_DATE_1, NOT_USED_A);
    AnnuityCouponFixed coupon = BOND_FIXED_SECURITY_DEFINITION.getCoupons().toDerivative(REFERENCE_DATE_1, NOT_USED_A);
    coupon = coupon.trimBefore(REFERENCE_TIME_1);
    MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(ISSUER_MULTICURVES, CUR, ISSUER);
    final MultipleCurrencyAmount pvNominal = PVDC.visit(nominal, multicurvesDecorated);
    final MultipleCurrencyAmount pvCoupon = PVDC.visit(coupon, multicurvesDecorated);
    final MultipleCurrencyAmount pv = METHOD_BOND_SECURITY.presentValue(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES);
    assertEquals("Fixed coupon bond security: present value", pvNominal.getAmount(CUR) + pvCoupon.getAmount(CUR), pv.getAmount(CUR), TOLERANCE_PV);
  }

  @Test
  public void presentValueFixedOnCoupon() {
    final AnnuityPaymentFixed nominal = (AnnuityPaymentFixed) BOND_FIXED_SECURITY_DEFINITION.getNominal().toDerivative(REFERENCE_DATE_2, NOT_USED_A);
    AnnuityCouponFixed coupon = BOND_FIXED_SECURITY_DEFINITION.getCoupons().toDerivative(REFERENCE_DATE_2, NOT_USED_A);
    coupon = coupon.trimBefore(REFERENCE_TIME_2);
    MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(ISSUER_MULTICURVES, CUR, ISSUER);
    final MultipleCurrencyAmount pvNominal = PVDC.visit(nominal, multicurvesDecorated);
    final MultipleCurrencyAmount pvCoupon = PVDC.visit(coupon, multicurvesDecorated);
    final MultipleCurrencyAmount pv = METHOD_BOND_SECURITY.presentValue(BOND_FIXED_SECURITY_2, ISSUER_MULTICURVES);
    assertEquals("Fixed coupon bond security: present value", pvNominal.getAmount(CUR) + pvCoupon.getAmount(CUR), pv.getAmount(CUR), TOLERANCE_PV);
  }

  @Test
  public void presentValueFixedMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD_BOND_SECURITY.presentValue(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES);
    final MultipleCurrencyAmount pvCalculator = PVIC.visit(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES);
    assertEquals("Fixed coupon bond security: present value Method vs Calculator", pvMethod.getAmount(CUR), pvCalculator.getAmount(CUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value from curves and a z-spread.
   */
  public void presentValueFromZSpread() {
    final MultipleCurrencyAmount pv = METHOD_BOND_SECURITY.presentValue(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES);
    double zSpread = 0.0;
    MultipleCurrencyAmount pvZ = METHOD_BOND_SECURITY.presentValueFromZSpread(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES, zSpread);
    assertEquals("Fixed coupon bond security: present value from z-spread", pv.getAmount(CUR), pvZ.getAmount(CUR), TOLERANCE_PV);
    IssuerProviderInterface issuerShifted = new IssuerProviderIssuerDecoratedSpread(ISSUER_MULTICURVES, BOND_FIXED_SECURITY_1.getIssuerCcy(), zSpread);
    MultipleCurrencyAmount pvZExpected = METHOD_BOND_SECURITY.presentValue(BOND_FIXED_SECURITY_1, issuerShifted);
    assertEquals("Fixed coupon bond security: present value from z-spread", pvZExpected.getAmount(CUR), pvZ.getAmount(CUR), TOLERANCE_PV);
    zSpread = 0.0010; // 10bps
    issuerShifted = new IssuerProviderIssuerDecoratedSpread(ISSUER_MULTICURVES, BOND_FIXED_SECURITY_1.getIssuerCcy(), zSpread);
    pvZ = METHOD_BOND_SECURITY.presentValueFromZSpread(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES, zSpread);
    pvZExpected = METHOD_BOND_SECURITY.presentValue(BOND_FIXED_SECURITY_1, issuerShifted);
    assertEquals("Fixed coupon bond security: present value from z-spread", pvZExpected.getAmount(CUR), pvZ.getAmount(CUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value z-spread sensitivity.
   */
  public void presentValueZSpreadSensitivity() {
    double zSpread = 0.0050; // 50bps
    double shift = 1.0E-5;
    double pvzs = METHOD_BOND_SECURITY.presentValueZSpreadSensitivity(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES, zSpread);
    final MultipleCurrencyAmount pvZUp = METHOD_BOND_SECURITY.presentValueFromZSpread(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES, zSpread + shift);
    final MultipleCurrencyAmount pvZDown = METHOD_BOND_SECURITY.presentValueFromZSpread(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES, zSpread - shift);
    assertEquals("Fixed coupon bond security: present value z-spread sensitivity", (pvZUp.getAmount(CUR) - pvZDown.getAmount(CUR)) / (2 * shift), pvzs, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests the bond security present value from clean price.
   */
  public void presentValueFromCleanPrice() {
    double cleanPrice = METHOD_BOND_SECURITY.cleanPriceFromCurves(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES);
    MultipleCurrencyAmount pvClean = METHOD_BOND_SECURITY.presentValueFromCleanPrice(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES.getMulticurveProvider(), cleanPrice);
    MultipleCurrencyAmount pvCleanExpected = METHOD_BOND_SECURITY.presentValue(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES);
    assertEquals("Fixed coupon bond security: present value", pvCleanExpected.getAmount(CUR), pvClean.getAmount(CUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the z-spread computation from the present value.
   */
  public void zSpreadFromPresentValue() {
    final MultipleCurrencyAmount pv = METHOD_BOND_SECURITY.presentValue(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES);
    double zSpread = METHOD_BOND_SECURITY.zSpreadFromCurvesAndPV(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES, pv);
    assertEquals("Fixed coupon bond security: present value from z-spread", 0.0, zSpread, TOLERANCE_PV);
    double zSpreadExpected = 0.0025; // 25bps
    MultipleCurrencyAmount pvZSpread = METHOD_BOND_SECURITY.presentValueFromZSpread(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES, zSpreadExpected);
    double zSpread2 = METHOD_BOND_SECURITY.zSpreadFromCurvesAndPV(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES, pvZSpread);
    assertEquals("Fixed coupon bond security: present value from z-spread", zSpreadExpected, zSpread2, TOLERANCE_PV);
    double zSpreadExpected3 = 0.0250; // 2.50%
    MultipleCurrencyAmount pvZSpread3 = METHOD_BOND_SECURITY.presentValueFromZSpread(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES, zSpreadExpected3);
    double zSpread3 = METHOD_BOND_SECURITY.zSpreadFromCurvesAndPV(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES, pvZSpread3);
    assertEquals("Fixed coupon bond security: present value from z-spread", zSpreadExpected3, zSpread3, TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the z-spread sensitivity computation from the present value.
   */
  public void zSpreadSensitivityFromPresentValue() {
    double zSpread = 0.0025; // 25bps
    MultipleCurrencyAmount pvZSpread = METHOD_BOND_SECURITY.presentValueFromZSpread(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES, zSpread);
    double zsComputed = METHOD_BOND_SECURITY.presentValueZSpreadSensitivityFromCurvesAndPV(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES, pvZSpread);
    double zsExpected = METHOD_BOND_SECURITY.presentValueZSpreadSensitivity(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES, zSpread);
    assertEquals("Fixed coupon bond security: z-spread sensitivity", zsExpected, zsComputed, TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the z-spread computation from the clean price.
   */
  public void zSpreadFromCleanPrice() {
    double zSpreadExpected = 0.0025; // 25bps
    IssuerProviderInterface issuerShifted = new IssuerProviderIssuerDecoratedSpread(ISSUER_MULTICURVES, BOND_FIXED_SECURITY_1.getIssuerCcy(), zSpreadExpected);
    double cleanZSpread = METHOD_BOND_SECURITY.cleanPriceFromCurves(BOND_FIXED_SECURITY_1, issuerShifted);
    double zSpread = METHOD_BOND_SECURITY.zSpreadFromCurvesAndClean(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES, cleanZSpread);
    assertEquals("Fixed coupon bond security: present value from z-spread", zSpreadExpected, zSpread, TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the z-spread sensitivity computation from the present value.
   */
  public void zSpreadSensitivityFromCleanPrice() {
    double zSpread = 0.0025; // 25bps
    IssuerProviderInterface issuerShifted = new IssuerProviderIssuerDecoratedSpread(ISSUER_MULTICURVES, BOND_FIXED_SECURITY_1.getIssuerCcy(), zSpread);
    double cleanZSpread = METHOD_BOND_SECURITY.cleanPriceFromCurves(BOND_FIXED_SECURITY_1, issuerShifted);
    double zsComputed = METHOD_BOND_SECURITY.presentValueZSpreadSensitivityFromCurvesAndClean(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES, cleanZSpread);
    double zsExpected = METHOD_BOND_SECURITY.presentValueZSpreadSensitivity(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES, zSpread);
    assertEquals("Fixed coupon bond security: z-spread sensitivity", zsExpected, zsComputed, TOLERANCE_PV);
  }

  @Test
  public void dirtyPriceFixed() {
    final MultipleCurrencyAmount pv = METHOD_BOND_SECURITY.presentValue(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES);
    final double df = ISSUER_MULTICURVES.getMulticurveProvider().getDiscountFactor(CUR, REFERENCE_TIME_1);
    final double dirty = METHOD_BOND_SECURITY.dirtyPriceFromCurves(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES);
    assertEquals("Fixed coupon bond security: dirty price from curves", pv.getAmount(CUR) / df / BOND_FIXED_SECURITY_1.getCoupon().getNthPayment(0).getNotional(), dirty);
    assertTrue("Fixed coupon bond security: dirty price is relative price", (0.50 < dirty) & (dirty < 2.0));
  }

  //    @Test
  //    public void dirtyPriceFixedMethodVsCalculator() {
  //      final double dirtyMethod = METHOD_BOND_SECURITY.dirtyPriceFromCurves(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES);
  //      final DirtyPriceFromCurvesCalculator calculator = DirtyPriceFromCurvesCalculator.getInstance();
  //      final double dirtyCalculator = calculator.visit(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES);
  //      assertEquals("Fixed coupon bond security: dirty price from curves Method vs Calculator", dirtyMethod, dirtyCalculator);
  //    }

  @Test
  public void cleanPriceFixed() {
    final double dirty = METHOD_BOND_SECURITY.dirtyPriceFromCurves(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES);
    final double clean = METHOD_BOND_SECURITY.cleanPriceFromCurves(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES);
    assertEquals("Fixed coupon bond security: clean price from curves", dirty - BOND_FIXED_SECURITY_1.getAccruedInterest() / BOND_FIXED_SECURITY_1.getCoupon().getNthPayment(0).getNotional(), clean);
  }

  //    @Test
  //    public void cleanPriceFixedMethodVsCalculator() {
  //      final double cleanMethod = METHOD_BOND_SECURITY.cleanPriceFromCurves(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES);
  //      final CleanPriceFromCurvesCalculator calculator = CleanPriceFromCurvesCalculator.getInstance();
  //      final double cleanCalculator = calculator.visit(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES);
  //      assertEquals("Fixed coupon bond security: dirty price from curves Method vs Calculator", cleanMethod, cleanCalculator);
  //    }

  @Test
  public void cleanAndDirtyPriceFixed() {
    final double cleanPrice = 0.90;
    final double accruedInterest = BOND_FIXED_SECURITY_1.getAccruedInterest() / BOND_FIXED_SECURITY_1.getCoupon().getNthPayment(0).getNotional();
    assertEquals("Fixed coupon bond security", cleanPrice + accruedInterest, METHOD_BOND_SECURITY.dirtyPriceFromCleanPrice(BOND_FIXED_SECURITY_1, cleanPrice));
    final double dirtyPrice = 0.95;
    assertEquals("Fixed coupon bond security", dirtyPrice - accruedInterest, METHOD_BOND_SECURITY.cleanPriceFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice));
    assertEquals("Fixed coupon bond security", cleanPrice,
        METHOD_BOND_SECURITY.cleanPriceFromDirtyPrice(BOND_FIXED_SECURITY_1, METHOD_BOND_SECURITY.dirtyPriceFromCleanPrice(BOND_FIXED_SECURITY_1, cleanPrice)));
  }

  @Test
  public void dirtyPriceFromYieldUSStreet() {
    final double yield = 0.04;
    final double dirtyPrice = METHOD_BOND_SECURITY.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield);
    final double dirtyPriceExpected = 1.04173525; // To be check with another source.
    assertEquals("Fixed coupon bond security: dirty price from yield", dirtyPriceExpected, dirtyPrice, TOLERANCE_PRICE);
  }

  @Test
  public void cleanPriceFromYieldUSStreet() {
    final double yield = 0.04;
    final double dirtyPrice = METHOD_BOND_SECURITY.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield);
    final double cleanPriceExpected = METHOD_BOND_SECURITY.cleanPriceFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice);
    final double cleanPrice = METHOD_BOND_SECURITY.cleanPriceFromYield(BOND_FIXED_SECURITY_1, yield);
    assertEquals("Fixed coupon bond security: dirty price from yield", cleanPriceExpected, cleanPrice, TOLERANCE_PRICE);
  }

  @Test
  public void dirtyPriceFromYieldUSStreetLastPeriod() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2016, 6, 3); // In last period
    final BondFixedSecurity bondSecurity = BOND_FIXED_SECURITY_DEFINITION.toDerivative(referenceDate, NOT_USED_A);
    final double yield = 0.04;
    final double dirtyPrice = METHOD_BOND_SECURITY.dirtyPriceFromYield(bondSecurity, yield);
    final double dirtyPriceExpected = (1 + RATE_FIXED / COUPON_PER_YEAR) / (1 + bondSecurity.getAccrualFactorToNextCoupon() * yield / COUPON_PER_YEAR);
    assertEquals("Fixed coupon bond security: dirty price from yield US Street - last period", dirtyPriceExpected, dirtyPrice, TOLERANCE_PRICE);
  }

  @Test
  public void yieldFromDirtyPriceUSStreet() {
    final double yield = 0.04;
    final double dirtyPrice = METHOD_BOND_SECURITY.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield);
    final double yieldComputed = METHOD_BOND_SECURITY.yieldFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice);
    assertEquals("Fixed coupon bond security: yield from dirty price", yield, yieldComputed, TOLERANCE_YIELD);
  }

  @Test
  public void yieldFromCurvesUSStreet() {
    final double dirtyPrice = METHOD_BOND_SECURITY.dirtyPriceFromCurves(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES);
    final double yieldExpected = METHOD_BOND_SECURITY.yieldFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice);
    final double yieldComputed = METHOD_BOND_SECURITY.yieldFromCurves(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES);
    assertEquals("Fixed coupon bond security: yield from dirty price", yieldExpected, yieldComputed, TOLERANCE_YIELD);
  }

  @Test
  public void yieldFromCleanPriceUSStreet() {
    final double yield = 0.04;
    final double dirtyPrice = METHOD_BOND_SECURITY.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield);
    final double cleanPrice = METHOD_BOND_SECURITY.cleanPriceFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice);
    final double yieldComputed = METHOD_BOND_SECURITY.yieldFromCleanPrice(BOND_FIXED_SECURITY_1, cleanPrice);
    assertEquals("Fixed coupon bond security: yield from clean price", yield, yieldComputed, TOLERANCE_YIELD);
    final double cleanPrice2 = METHOD_BOND_SECURITY.cleanPriceFromYield(BOND_FIXED_SECURITY_1, yieldComputed);
    assertEquals("Fixed coupon bond security: yield from clean price", cleanPrice, cleanPrice2, TOLERANCE_YIELD);
  }

  @Test
  public void modifiedDurationFromYieldUSStreet() {
    final double yield = 0.04;
    final double modifiedDuration = METHOD_BOND_SECURITY.modifiedDurationFromYield(BOND_FIXED_SECURITY_1, yield);
    final double modifiedDurationExpected = 4.566199225; // To be check with another source.
    assertEquals("Fixed coupon bond security: modified duration from yield US Street - hard coded value", modifiedDurationExpected, modifiedDuration, TOLERANCE_PRICE);
    final double shift = 1.0E-6;
    final double dirty = METHOD_BOND_SECURITY.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield);
    final double dirtyP = METHOD_BOND_SECURITY.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield + shift);
    final double dirtyM = METHOD_BOND_SECURITY.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield - shift);
    final double modifiedDurationFD = -(dirtyP - dirtyM) / (2 * shift) / dirty;
    assertEquals("Fixed coupon bond security: modified duration from yield US Street - finite difference", modifiedDurationFD, modifiedDuration, TOLERANCE_PRICE);
  }

  @Test
  public void modifiedDurationFromCurvesUSStreet() {
    final double yield = METHOD_BOND_SECURITY.yieldFromCurves(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES);
    final double modifiedDurationExpected = METHOD_BOND_SECURITY.modifiedDurationFromYield(BOND_FIXED_SECURITY_1, yield);
    final double modifiedDuration = METHOD_BOND_SECURITY.modifiedDurationFromCurves(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES);
    assertEquals("Fixed coupon bond security: modified duration from curves US Street", modifiedDurationExpected, modifiedDuration, TOLERANCE_PRICE);
  }

  @Test
  public void modifiedDurationFromDirtyPriceUSStreet() {
    final double dirtyPrice = 0.95;
    final double yield = METHOD_BOND_SECURITY.yieldFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice);
    final double modifiedDurationExpected = METHOD_BOND_SECURITY.modifiedDurationFromYield(BOND_FIXED_SECURITY_1, yield);
    final double modifiedDuration = METHOD_BOND_SECURITY.modifiedDurationFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice);
    assertEquals("Fixed coupon bond security: modified duration from curves US Street", modifiedDurationExpected, modifiedDuration, TOLERANCE_PRICE);
  }

  @Test
  public void modifiedDurationFromYieldUSStreetLastPeriod() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2016, 6, 3); // In last period
    final BondFixedSecurity bondSecurity = BOND_FIXED_SECURITY_DEFINITION.toDerivative(referenceDate, NOT_USED_A);
    final double yield = 0.04;
    final double dirtyPrice = METHOD_BOND_SECURITY.modifiedDurationFromYield(bondSecurity, yield);
    final double dirtyPriceExpected = bondSecurity.getAccrualFactorToNextCoupon() / COUPON_PER_YEAR / (1 + bondSecurity.getAccrualFactorToNextCoupon() * yield / COUPON_PER_YEAR);
    assertEquals("Fixed coupon bond security: modified duration from yield US Street - last period", dirtyPriceExpected, dirtyPrice, TOLERANCE_PRICE);
  }

  //    @Test
  //    public void modifiedDurationFromCurvesUSStreetMethodVsCalculator() {
  //      final double mdMethod = METHOD_BOND_SECURITY.modifiedDurationFromCurves(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES);
  //      final ModifiedDurationFromCurvesCalculator calculator = ModifiedDurationFromCurvesCalculator.getInstance();
  //      final double mdCalculator = calculator.visit(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES);
  //      assertEquals("Fixed coupon bond security: modified duration from curves US Street : Method vs Calculator", mdMethod, mdCalculator, 1E-8);
  //    }

  @Test
  /**
   * Tests Macauley duration vs a hard coded value (US Street convention).
   */
  public void macauleyDurationFromYieldUSStreet() {
    final double yield = 0.04;
    final double mc = METHOD_BOND_SECURITY.macaulayDurationFromYield(BOND_FIXED_SECURITY_1, yield);
    double dirty = METHOD_BOND_SECURITY.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield);
    double mcExpected = 4.851906106 / dirty;
    assertEquals("Fixed coupon bond security: Macauley duration from yield US Street: harcoded value", mcExpected, mc, 1E-8);
    final double md = METHOD_BOND_SECURITY.modifiedDurationFromYield(BOND_FIXED_SECURITY_1, yield);
    assertEquals("Fixed coupon bond security: Macauley duration from yield US Street: vs modified duration", md * (1 + yield / COUPON_PER_YEAR), mc, 1E-8);
  }

  @Test
  /**
   * Tests Macauley duration from the curves (US Street convention).
   */
  public void macauleyDurationFromCurvesUSStreet() {
    final double yield = METHOD_BOND_SECURITY.yieldFromCurves(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES);
    final double macauleyDurationExpected = METHOD_BOND_SECURITY.macaulayDurationFromYield(BOND_FIXED_SECURITY_1, yield);
    final double macauleyDuration = METHOD_BOND_SECURITY.macaulayDurationFromCurves(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES);
    assertEquals("Fixed coupon bond security: Macauley duration from curves US Street", macauleyDurationExpected, macauleyDuration, 1E-8);
  }

  @Test
  /**
   * Tests Macauley duration from a dirty price (US Street convention).
   */
  public void macauleyDurationFromDirtyPriceUSStreet() {
    final double dirtyPrice = 0.95;
    final double yield = METHOD_BOND_SECURITY.yieldFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice);
    final double macauleyDurationExpected = METHOD_BOND_SECURITY.macaulayDurationFromYield(BOND_FIXED_SECURITY_1, yield);
    final double macauleyDuration = METHOD_BOND_SECURITY.macaulayDurationFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice);
    assertEquals("Fixed coupon bond security: Macauley duration from curves US Street", macauleyDurationExpected, macauleyDuration, 1E-8);
  }

  //    @Test
  //    /**
  //     * Tests Macauley duration: Method vs Calculator (US Street convention).
  //     */
  //    public void macauleyDurationFromCurvesUSStreetMethodVsCalculator() {
  //      final double mcMethod = METHOD_BOND_SECURITY.macaulayDurationFromCurves(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES);
  //      final MacaulayDurationFromCurvesCalculator calculator = MacaulayDurationFromCurvesCalculator.getInstance();
  //      final double mcCalculator = calculator.visit(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES);
  //      assertEquals("Fixed coupon bond security: Macauley duration from curves US Street : Method vs Calculator", mcMethod, mcCalculator, 1E-8);
  //    }

  @Test
  /**
   * Tests convexity vs a hard coded value (US Street convention).
   */
  public void convexityDurationFromYieldUSStreet() {
    final double yield = 0.04;
    final double cv = METHOD_BOND_SECURITY.convexityFromYield(BOND_FIXED_SECURITY_1, yield);
    double dirty = METHOD_BOND_SECURITY.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield);
    double cvExpected = 25.75957016 / dirty;
    assertEquals("Fixed coupon bond security: Macauley duration from yield US Street: harcoded value", cvExpected, cv, 1E-8);
    final double shift = 1.0E-6;
    final double dirtyP = METHOD_BOND_SECURITY.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield + shift);
    final double dirtyM = METHOD_BOND_SECURITY.dirtyPriceFromYield(BOND_FIXED_SECURITY_1, yield - shift);
    final double cvFD = (dirtyP + dirtyM - 2 * dirty) / (shift * shift) / dirty;
    assertEquals("Fixed coupon bond security: modified duration from yield US Street - finite difference", cvFD, cv, 1E-2);
  }

  @Test
  /**
   * Tests convexity from the curves (US Street convention).
   */
  public void convexityFromCurvesUSStreet() {
    final double yield = METHOD_BOND_SECURITY.yieldFromCurves(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES);
    final double convexityExpected = METHOD_BOND_SECURITY.convexityFromYield(BOND_FIXED_SECURITY_1, yield);
    final double convexity = METHOD_BOND_SECURITY.convexityFromCurves(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES);
    assertEquals("Fixed coupon bond security: convexity from curves US Street", convexityExpected, convexity, 1E-8);
  }

  @Test
  /**
   * Tests convexity from a dirty price (US Street convention).
   */
  public void convexityFromDirtyPriceUSStreet() {
    final double dirtyPrice = 0.95;
    final double yield = METHOD_BOND_SECURITY.yieldFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice);
    final double convexityExpected = METHOD_BOND_SECURITY.convexityFromYield(BOND_FIXED_SECURITY_1, yield);
    final double convexity = METHOD_BOND_SECURITY.convexityFromDirtyPrice(BOND_FIXED_SECURITY_1, dirtyPrice);
    assertEquals("Fixed coupon bond security: convexity from curves US Street", convexityExpected, convexity, 1E-8);
  }

  @Test
  public void dirtyPriceCurveSensitivity() {
    MulticurveSensitivity sensi = METHOD_BOND_SECURITY.dirtyPriceCurveSensitivity(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES).cleaned();
    sensi = sensi.cleaned();
    final MultipleCurrencyAmount pv = METHOD_BOND_SECURITY.presentValue(BOND_FIXED_SECURITY_1, ISSUER_MULTICURVES);
    final double dfSettle = ISSUER_MULTICURVES.getMulticurveProvider().getDiscountFactor(CUR, BOND_FIXED_SECURITY_1.getSettlementTime());
    String DSC_CURVE_NAME = ISSUER_MULTICURVES.getMulticurveProvider().getName(CUR);
    String ISS_CURVE_NAME = ISSUER_MULTICURVES.getName(BOND_FIXED_SECURITY_1.getIssuerCcy());
    assertEquals("Fixed coupon bond security: dirty price curve sensitivity: risk-less curve", BOND_FIXED_SECURITY_1.getSettlementTime(), sensi.getYieldDiscountingSensitivities().get(DSC_CURVE_NAME)
        .get(0).first, TOLERANCE_PV_DELTA);
    assertEquals("Fixed coupon bond security: dirty price curve sensitivity: risk-less curve", BOND_FIXED_SECURITY_1.getSettlementTime() / dfSettle * pv.getAmount(CUR) / NOTIONAL, sensi
        .getYieldDiscountingSensitivities().get(DSC_CURVE_NAME).get(0).second, TOLERANCE_PV_DELTA);
    final double dfCpn0 = ISSUER_MULTICURVES.getMulticurveProvider().getDiscountFactor(BOND_FIXED_SECURITY_1.getCurrency(), BOND_FIXED_SECURITY_1.getCoupon().getNthPayment(0).getPaymentTime());
    assertEquals("Fixed coupon bond security: dirty price curve sensitivity: repo curve", BOND_FIXED_SECURITY_1.getCoupon().getNthPayment(0).getPaymentTime(), sensi.getYieldDiscountingSensitivities()
        .get(ISS_CURVE_NAME).get(0).first, TOLERANCE_PV_DELTA);
    assertEquals("Fixed coupon bond security: dirty price curve sensitivity: repo curve", -BOND_FIXED_SECURITY_1.getCoupon().getNthPayment(0).getPaymentTime()
        * BOND_FIXED_SECURITY_1.getCoupon().getNthPayment(0).getAmount() * dfCpn0 / dfSettle / NOTIONAL, sensi.getYieldDiscountingSensitivities().get(ISS_CURVE_NAME).get(0).second, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests that the clean price for consecutive dates in the future are relatively smooth (no jump die to miscalculated accrued or missing coupon).
   */
  public void cleanPriceSmoothness() {
    final int nbDateForward = 150;
    final ZonedDateTime[] forwardDate = new ZonedDateTime[nbDateForward];
    forwardDate[0] = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_2, SETTLEMENT_DAYS, CALENDAR); //Spot
    final long[] jumpDays = new long[nbDateForward - 1];
    for (int loopdate = 1; loopdate < nbDateForward; loopdate++) {
      forwardDate[loopdate] = ScheduleCalculator.getAdjustedDate(forwardDate[loopdate - 1], 1, CALENDAR);
      jumpDays[loopdate - 1] = forwardDate[loopdate].toLocalDate().toModifiedJulianDays() - forwardDate[loopdate - 1].toLocalDate().toModifiedJulianDays();
    }
    final double[] cleanPriceForward = new double[nbDateForward];
    for (int loopdate = 0; loopdate < nbDateForward; loopdate++) {
      if (loopdate == 87) {
        int t = 0;
        t++;
      }
      final BondFixedSecurity bondForward = BOND_FIXED_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE_2, forwardDate[loopdate], NOT_USED_A);
      cleanPriceForward[loopdate] = METHOD_BOND_SECURITY.cleanPriceFromCurves(bondForward, ISSUER_MULTICURVES);
    }
    //Test note: 0.03425 is roughly the difference between the coupon and the risk/free rate. The clean price is decreasing naturally by this amount divided by (roughly) 365 every day.
    //Test note: On the coupon date there is a jump in the clean price: If the coupon is included the clean price due to coupon is 0.04625/2*exp(-t*0.0100)*exp(t*0.0120) - 0.04625/2 = 1.59276E-05; 
    //           if the coupon is not included the impact is 0 (t=?). The clean price is thus expected to jump by the above amount when the settlement is on the coupon date 15-May-2012.
    final double couponJump = 1.59276E-05;
    for (int loopdate = 1; loopdate < nbDateForward; loopdate++) {
      assertEquals("Fixed coupon bond security: clean price smoothness " + loopdate, cleanPriceForward[loopdate] - (loopdate == 87 ? couponJump : 0.0), cleanPriceForward[loopdate - 1]
          - jumpDays[loopdate - 1] * (0.03425 / 365.0), 3.0E-5);
    }
  }

  // UKT 5 09/07/14 - ISIN-GB0031829509 To check figures in the ex-dividend period
  private static final String ISSUER_UK = ISSUER_NAMES[3];
  private static final Currency GBP = Currency.GBP;
  private static final Period PAYMENT_TENOR_UK = Period.ofMonths(6);
  private static final int COUPON_PER_YEAR_G = 2;
  private static final Calendar CALENDAR_UK = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_UK = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"); // To check
  private static final BusinessDayConvention BUSINESS_DAY_UK = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final boolean IS_EOM_UK = false;
  private static final Period BOND_TENOR_G = Period.ofYears(12);
  private static final int SETTLEMENT_DAYS_UK = 1;
  private static final int EX_DIVIDEND_DAYS_UK = 7;
  private static final ZonedDateTime START_ACCRUAL_DATE_UK = DateUtils.getUTCDate(2002, 9, 7);
  private static final ZonedDateTime MATURITY_DATE_UK = START_ACCRUAL_DATE_UK.plus(BOND_TENOR_G);
  private static final double RATE_UK = 0.0500;
  private static final double NOTIONAL_UK = 100;
  private static final YieldConvention YIELD_CONVENTION_UK = YieldConventionFactory.INSTANCE.getYieldConvention("UK:BUMP/DMO METHOD");
  private static final BondFixedSecurityDefinition BOND_FIXED_SECURITY_DEFINITION_UK = BondFixedSecurityDefinition.from(GBP, MATURITY_DATE_UK, START_ACCRUAL_DATE_UK, PAYMENT_TENOR_UK, RATE_UK,
      SETTLEMENT_DAYS_UK, NOTIONAL_UK, EX_DIVIDEND_DAYS_UK, CALENDAR_UK, DAY_COUNT_UK, BUSINESS_DAY_UK, YIELD_CONVENTION_UK, IS_EOM_UK, ISSUER_UK, NOT_USED);
  private static final ZonedDateTime REFERENCE_DATE_3 = DateUtils.getUTCDate(2011, 9, 2); // Ex-dividend is 30-Aug-2011
  private static final BondFixedSecurity BOND_FIXED_SECURITY_UK = BOND_FIXED_SECURITY_DEFINITION_UK.toDerivative(REFERENCE_DATE_3, NOT_USED_A);
  private static final ZonedDateTime SPOT_3 = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE_3, SETTLEMENT_DAYS_UK, CALENDAR);
  private static final double REFERENCE_TIME_3 = TimeCalculator.getTimeBetween(REFERENCE_DATE_3, SPOT_3);

  @Test
  public void presentValueFixedExDividend() {
    final MultipleCurrencyAmount pv = METHOD_BOND_SECURITY.presentValue(BOND_FIXED_SECURITY_UK, ISSUER_MULTICURVES);
    final BondFixedSecurityDefinition bondNoExDefinition = BondFixedSecurityDefinition.from(GBP, MATURITY_DATE_UK, START_ACCRUAL_DATE_UK, PAYMENT_TENOR_UK, RATE_UK, SETTLEMENT_DAYS_UK, NOTIONAL_UK,
        0, CALENDAR_UK, DAY_COUNT_UK, BUSINESS_DAY_UK, YIELD_CONVENTION_UK, IS_EOM_UK, ISSUER_UK, NOT_USED);
    final BondFixedSecurity BondNoEx = bondNoExDefinition.toDerivative(REFERENCE_DATE_3, NOT_USED_A);
    final MultipleCurrencyAmount pvNoEx = METHOD_BOND_SECURITY.presentValue(BondNoEx, ISSUER_MULTICURVES);
    final CouponFixedDefinition couponDefinitionEx = BOND_FIXED_SECURITY_DEFINITION_UK.getCoupons().getNthPayment(17);
    MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(ISSUER_MULTICURVES, GBP, ISSUER_UK);
    final MultipleCurrencyAmount pvCpn = PVDC.visit(couponDefinitionEx.toDerivative(REFERENCE_DATE_3, NOT_USED_A), multicurvesDecorated);
    assertEquals("Fixed coupon bond security: present value ex dividend", pvNoEx.getAmount(GBP) - pvCpn.getAmount(GBP), pv.getAmount(GBP), TOLERANCE_PV);
  }

  @Test
  public void dirtyPriceFixedExDividend() {
    final MultipleCurrencyAmount pv = METHOD_BOND_SECURITY.presentValue(BOND_FIXED_SECURITY_UK, ISSUER_MULTICURVES);
    final double df = ISSUER_MULTICURVES.getMulticurveProvider().getDiscountFactor(GBP, REFERENCE_TIME_3);
    final double dirty = METHOD_BOND_SECURITY.dirtyPriceFromCurves(BOND_FIXED_SECURITY_UK, ISSUER_MULTICURVES);
    assertEquals("Fixed coupon bond security: dirty price from curves", pv.getAmount(GBP) / df / BOND_FIXED_SECURITY_UK.getCoupon().getNthPayment(0).getNotional(), dirty);
    assertTrue("Fixed coupon bond security: dirty price is relative price", (0.50 < dirty) & (dirty < 2.0));
  }

  @Test
  public void dirtyPriceFromYieldUKExDividend() {
    final double yield = 0.04;
    final double dirtyPrice = METHOD_BOND_SECURITY.dirtyPriceFromYield(BOND_FIXED_SECURITY_UK, yield);
    final double dirtyPriceExpected = 1.0277859038; // To be check with another source.
    assertEquals("Fixed coupon bond security: dirty price from yield UK", dirtyPriceExpected, dirtyPrice, 1E-8);
  }

  @Test
  public void dirtyPriceFromYieldUKLastPeriod() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2014, 6, 3); // In last period
    final BondFixedSecurity bondSecurity = BOND_FIXED_SECURITY_DEFINITION_UK.toDerivative(referenceDate, NOT_USED_A);
    final double yield = 0.04;
    final double dirtyPrice = METHOD_BOND_SECURITY.dirtyPriceFromYield(bondSecurity, yield);
    final double dirtyPriceExpected = (1 + RATE_UK / COUPON_PER_YEAR_G) * Math.pow(1 + yield / COUPON_PER_YEAR_G, -bondSecurity.getAccrualFactorToNextCoupon());
    assertEquals("Fixed coupon bond security: dirty price from yield UK - last period", dirtyPriceExpected, dirtyPrice, 1E-8);
  }

  @Test
  public void yieldFromDirtyPriceUKExDividend() {
    final double yield = 0.04;
    final double dirtyPrice = METHOD_BOND_SECURITY.dirtyPriceFromYield(BOND_FIXED_SECURITY_UK, yield);
    final double yieldComputed = METHOD_BOND_SECURITY.yieldFromDirtyPrice(BOND_FIXED_SECURITY_UK, dirtyPrice);
    assertEquals("Fixed coupon bond security: yield from dirty price UK", yield, yieldComputed, 1E-10);
  }

  @Test
  public void modifiedDurationFromYieldUKExDividend() {
    final double yield = 0.04;
    final double modifiedDuration = METHOD_BOND_SECURITY.modifiedDurationFromYield(BOND_FIXED_SECURITY_UK, yield);
    final double modifiedDurationExpected = 2.7757118292; // To be check with another source.
    assertEquals("Fixed coupon bond security: modified duration from yield UK DMO - hard coded value", modifiedDurationExpected, modifiedDuration, 1E-8);
    final double shift = 1.0E-6;
    final double dirty = METHOD_BOND_SECURITY.dirtyPriceFromYield(BOND_FIXED_SECURITY_UK, yield);
    final double dirtyP = METHOD_BOND_SECURITY.dirtyPriceFromYield(BOND_FIXED_SECURITY_UK, yield + shift);
    final double dirtyM = METHOD_BOND_SECURITY.dirtyPriceFromYield(BOND_FIXED_SECURITY_UK, yield - shift);
    final double modifiedDurationFD = -(dirtyP - dirtyM) / (2 * shift) / dirty;
    assertEquals("Fixed coupon bond security: modified duration from yield UK DMO - finite difference", modifiedDurationFD, modifiedDuration, 1E-8);
  }

  @Test
  public void macauleyDurationFromYieldUKExDividend() {
    final double yield = 0.04;
    final double macauleyDuration = METHOD_BOND_SECURITY.macaulayDurationFromYield(BOND_FIXED_SECURITY_UK, yield);
    final double macauleyDurationExpected = 2.909894241 / METHOD_BOND_SECURITY.dirtyPriceFromYield(BOND_FIXED_SECURITY_UK, yield); // To be check with another source.
    assertEquals("Fixed coupon bond security: Macauley duration from yield UK DMO - hard coded value", macauleyDurationExpected, macauleyDuration, 1E-8);
  }

  // UKT 6 1/4 11/25/10
  private static final DayCount DAY_COUNT_G2 = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA"); // To check
  //  private static final Period BOND_TENOR_G2 = Period.ofYears(10);
  private static final int SETTLEMENT_DAYS_G2 = 1;
  private static final int EX_DIVIDEND_DAYS_G2 = 7;
  private static final ZonedDateTime START_ACCRUAL_DATE_G2 = DateUtils.getUTCDate(1999, 11, 25);
  private static final ZonedDateTime MATURITY_DATE_G2 = DateUtils.getUTCDate(2010, 11, 25);
  private static final double RATE_G2 = 0.0625;
  private static final double NOTIONAL_G2 = 100;
  private static final YieldConvention YIELD_CONVENTION_G2 = YieldConventionFactory.INSTANCE.getYieldConvention("UK:BUMP/DMO METHOD");
  private static final BondFixedSecurityDefinition BOND_FIXED_SECURITY_DEFINITION_G2 = BondFixedSecurityDefinition.from(GBP, MATURITY_DATE_G2, START_ACCRUAL_DATE_G2, PAYMENT_TENOR_UK, RATE_G2,
      SETTLEMENT_DAYS_G2, NOTIONAL_G2, EX_DIVIDEND_DAYS_G2, CALENDAR_UK, DAY_COUNT_G2, BUSINESS_DAY_UK, YIELD_CONVENTION_G2, IS_EOM_UK, ISSUER_UK, "");
  private static final ZonedDateTime REFERENCE_DATE_4 = DateUtils.getUTCDate(2001, 8, 10);
  private static final BondFixedSecurity BOND_FIXED_SECURITY_G2 = BOND_FIXED_SECURITY_DEFINITION_G2.toDerivative(REFERENCE_DATE_4, NOT_USED_A);

  @Test
  public void dirtyPriceFromCleanPriceUK() {
    final double cleanPrice = 110.20 / 100.00;
    final double dirtyPrice = METHOD_BOND_SECURITY.dirtyPriceFromCleanPrice(BOND_FIXED_SECURITY_G2, cleanPrice);
    final double dirtyPriceExpected = 1.11558696;
    assertEquals("Fixed coupon bond security: dirty price from clean price", dirtyPriceExpected, dirtyPrice, 1E-8);
  }

  @Test
  public void yieldPriceFromCleanPriceUK() {
    final double cleanPrice = 110.20 / 100.00;
    final double yield = METHOD_BOND_SECURITY.yieldFromCleanPrice(BOND_FIXED_SECURITY_G2, cleanPrice);
    final double yieldExpected = 0.04870;
    assertEquals("Fixed coupon bond security: dirty price from clean price", yieldExpected, yield, 1E-5);
  }

  @Test
  public void modifiedDurationFromCleanPriceUK() {
    final double cleanPrice = 110.20 / 100.00;
    final double dirtyPrice = METHOD_BOND_SECURITY.dirtyPriceFromCleanPrice(BOND_FIXED_SECURITY_G2, cleanPrice);
    final double md = METHOD_BOND_SECURITY.modifiedDurationFromDirtyPrice(BOND_FIXED_SECURITY_G2, dirtyPrice);
    final double mdExpected = 7.039;
    assertEquals("Fixed coupon bond security: dirty price from clean price", mdExpected, md, 1E-3);
  }

}
