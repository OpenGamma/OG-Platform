/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.options.YieldCurveProvider;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCounts;

/**
 * 
 */
public class AnalyticBondPricerTest extends ISDABaseTest {

  private static boolean PRINT = false;
  static {
    if (PRINT) {
      System.out.println("BondEquivalentCDSSpreadTest - Set PRINT to false");
    }
  }

  @Test
  public void bondPriceTest() {

    final double recoveryRate = 0.4;
    final CDSAnalyticFactory factory = new CDSAnalyticFactory(recoveryRate);
    final LocalDate tradeDate = LocalDate.of(2014, 3, 14);
    final CDSAnalytic cds = factory.makeIMMCDS(tradeDate, Period.ofYears(5));

    final double bondCoupon = 0.05;
    //for now use the CDS mechanics to generate bond payment schedule 
    final CDSAnalytic dummyCDS = factory.with(Period.ofMonths(6)).withProtectionStart(false).makeIMMCDS(tradeDate, Period.ofYears(5));
    final CDSCoupon[] cdsCoupons = dummyCDS.getCoupons();
    final int n = dummyCDS.getNumPayments();
    final double[] paymentTimes = new double[n];
    final double[] paymentAmounts = new double[n];

    for (int i = 0; i < n; i++) {
      paymentTimes[i] = cdsCoupons[i].getPaymentTime();
      paymentAmounts[i] = cdsCoupons[i].getYearFrac() * bondCoupon;
    }
    paymentAmounts[n - 1] += 1.0;
    final BondAnalytic bond = new BondAnalytic(paymentTimes, paymentAmounts, recoveryRate, dummyCDS.getAccruedPremium(bondCoupon));

    final ISDACompliantYieldCurve yieldCurve = new ISDACompliantYieldCurve(1.0, 0.05);
    final double cleanPrice = defaultFreeBondCleanPrice(bond, yieldCurve);

    final AnalyticBondPricer bondSpreadCal = new AnalyticBondPricer();
    final Function1D<Double, Double> bondPriceFunc = bondSpreadCal.getBondPriceForHazardRateFunction(bond, yieldCurve, PriceType.CLEAN);
    //now price will zero hazard rate - should get same number
    final double price = bondPriceFunc.evaluate(0.0);
    assertEquals(cleanPrice, price, 1e-15);

    assertEquals("Hazard rate limit", recoveryRate, bondSpreadCal.bondPriceForHazardRate(bond, yieldCurve, 1000.0, PriceType.DIRTY), 2e-5);

    //ramp up the hazard rate
    if (PRINT) {
      for (int i = 0; i < 100; i++) {
        final double lambda = i * 1.0 / 99;
        final double rPrice = bondPriceFunc.evaluate(lambda);
        final double s = bondSpreadCal.getEquivalentCDSSpread(bond, yieldCurve, rPrice, PriceType.CLEAN, cds);
        System.out.println(lambda + "\t" + rPrice + "\t" + s);
      }
    }
  }

  /**
   * Check our bond price is consistent with the CDS price. To do this we must price the protection leg of a CDS with protection from start true, but the annuity with
   * protection from start false (the annuity must also not have accrual-on-default)
   */
  @Test
  public void bondPriceTest2() {

    final ISDACompliantYieldCurve yieldCurve = YieldCurveProvider.ISDA_USD_20140205;

    final double recoveryRate = 0.27;

    final Period couponPeriod = Period.ofMonths(6);
    final StubType stub = StubType.FRONTSHORT;
    final BusinessDayConvention bd = FOLLOWING;
    final Calendar cal = DEFAULT_CALENDAR;
    final CDSAnalyticFactory factory = new CDSAnalyticFactory(0.0).with(couponPeriod).withPayAccOnDefault(false);

    final LocalDate startDate = LocalDate.of(2013, 9, 20);
    final LocalDate endDate = LocalDate.of(2019, 3, 20);
    final LocalDate tradeDate = LocalDate.of(2014, 2, 5);
    final double exp = ACT365F.getDayCountFraction(tradeDate, endDate);

    final CDSAnalytic protectionLegCDS = factory.makeCDS(tradeDate, startDate, endDate);
    final CDSAnalytic annuityCDS = factory.withProtectionStart(false).makeCDS(tradeDate, startDate, endDate);

    final double bondCoupon = 0.07;
    final ISDAPremiumLegSchedule sch = new ISDAPremiumLegSchedule(startDate, endDate, couponPeriod, stub, bd, cal, false);
    final BondAnalytic bond = new BondAnalytic(tradeDate, bondCoupon, sch, recoveryRate, ACT360);

    final AnalyticCDSPricer cdsPricer = new AnalyticCDSPricer();
    final AnalyticBondPricer bondPricer = new AnalyticBondPricer();

    for (int i = 0; i < 10; i++) {
      final double lambda = 0.0 + 0.3 * i / 9.;
      final ISDACompliantCreditCurve cc = new ISDACompliantCreditCurve(10.0, lambda);
      final double bondPrice = bondPricer.bondPriceForHazardRate(bond, yieldCurve, lambda, PriceType.DIRTY);
      final double cdsProtLeg = cdsPricer.protectionLeg(protectionLegCDS, yieldCurve, cc, 0.0);
      final double cdsAnnuity = cdsPricer.annuity(annuityCDS, yieldCurve, cc, PriceType.DIRTY, 0.0);
      final double q = cc.getSurvivalProbability(exp);
      final double p = yieldCurve.getDiscountFactor(exp);
      final double bondPriceAsCDS = cdsAnnuity * bondCoupon + q * p + recoveryRate * cdsProtLeg;
      //    System.out.println(cdsProtLeg);
      //    System.out.println(bondPrice + "\t" + bondPriceAsCDS);
      assertEquals(bondPriceAsCDS, bondPrice, 1e-15);
    }
  }

  /**
   * Bond and CDS coincide for certain setup
   */
  public void limitedCaseTest() {
    final double tol = 1.e-12;
    final AnalyticBondPricer bondPricer = new AnalyticBondPricer();
    final AnalyticCDSPricer cdsPricer = new AnalyticCDSPricer();

    final LocalDate tradeDate = LocalDate.of(2014, 2, 13);
    final LocalDate startDate = LocalDate.of(2013, 12, 20);
    final LocalDate endDate = LocalDate.of(2018, 12, 20);

    final Period couponPrd = Period.ofMonths(6);
    final StubType stubTp = StubType.FRONTSHORT;
    final BusinessDayConvention bdConv = MOD_FOLLOWING;
    final Calendar cal = DEFAULT_CALENDAR;
    boolean ProtStart = false;
    double rr = 0.;
    final PriceType priceTp = PriceType.DIRTY;

    final ISDACompliantYieldCurve yc = YieldCurveProvider.ISDA_USD_20140213;
    final double hr = 0.2;
    final ISDACompliantCreditCurve cc = new ISDACompliantCreditCurve(10., hr);
    final double coupon = 0.1;
    final ISDAPremiumLegSchedule schedule = new ISDAPremiumLegSchedule(startDate, endDate, couponPrd, stubTp, bdConv, cal, ProtStart);
    final BondAnalytic bond = new BondAnalytic(tradeDate, coupon, schedule, rr, ACT360);
    final CDSAnalytic cds = new CDSAnalytic(tradeDate, tradeDate.plusDays(1), tradeDate, startDate, endDate, false, couponPrd, stubTp,
        ProtStart, 1. - rr, bdConv, cal, ACT360, DayCounts.ACT_365);

    final double resBond1 = bondPricer.bondPriceForHazardRate(bond, yc, hr, priceTp);
    final double resCDS1 = -cdsPricer.pv(cds, yc, cc, coupon, priceTp);
    final double mat = bond.getPaymentTime(bond.getnPayments() - 1);
    assertEquals((resCDS1 + 1. * yc.getDiscountFactor(mat) * cc.getDiscountFactor(mat)), resBond1, tol);
    final double eqSp1 = bondPricer.getEquivalentCDSSpread(bond, yc, resBond1, priceTp, cds);
    assertEquals(0., eqSp1, tol);

    rr = 0.3;
    ProtStart = true;
    final CDSAnalytic cds2 = new CDSAnalytic(tradeDate, tradeDate.plusDays(1), tradeDate, startDate, endDate, false, couponPrd, stubTp,
        ProtStart, 1. - rr, bdConv, cal, ACT360, DayCounts.ACT_365);
    final BondAnalytic bond2 = new BondAnalytic(tradeDate, 0., schedule, rr, ACT360);
    final double resBond2 = bondPricer.bondPriceForHazardRate(bond2, yc, hr, priceTp);
    final double resCDS2 = cdsPricer.pv(cds2, yc, cc, 0., priceTp);
    assertEquals((resCDS2 + 1. * yc.getDiscountFactor(mat) * cc.getDiscountFactor(mat)), resBond2, tol);
    final double eqSp2 = bondPricer.getEquivalentCDSSpread(bond2, yc, resBond2, priceTp, cds2);
    final double sp2 = cdsPricer.parSpread(cds2, yc, cc);
    assertEquals(sp2, eqSp2, tol);
  }

  /**
   * 
   */
  public void hazardRateTest() {
    final double tol = 1.e-12;
    final AnalyticBondPricer bondPricer = new AnalyticBondPricer();

    final LocalDate tradeDate = LocalDate.of(2014, 2, 13);
    final LocalDate startDate = LocalDate.of(2013, 12, 20);
    final LocalDate endDate = LocalDate.of(2018, 12, 20);
    final ISDACompliantYieldCurve yc = YieldCurveProvider.ISDA_USD_20140213;

    final double coupon = 0.11;
    double rr = 0.3;
    final Period couponPrd = Period.ofMonths(6);
    final StubType stubTp = StubType.FRONTSHORT;
    final BusinessDayConvention bdConv = MOD_FOLLOWING;
    final Calendar cal = DEFAULT_CALENDAR;
    final boolean ProtStart = true;
    final ISDAPremiumLegSchedule schedule = new ISDAPremiumLegSchedule(startDate, endDate, couponPrd, stubTp, bdConv, cal, ProtStart);
    final BondAnalytic bond = new BondAnalytic(tradeDate, coupon, schedule, rr, ACT360);

    final double hr = 0.15;
    final double cleanPrice = bondPricer.bondPriceForHazardRate(bond, yc, hr, PriceType.CLEAN);
    final double dirtyPrice = bondPricer.bondPriceForHazardRate(bond, yc, hr, PriceType.DIRTY);
    final double hrClean = bondPricer.getHazardRate(bond, yc, cleanPrice, PriceType.CLEAN);
    final double hrDirty = bondPricer.getHazardRate(bond, yc, dirtyPrice, PriceType.DIRTY);
    assertEquals(hr, hrClean, tol);
    assertEquals(hr, hrDirty, tol);

    final double priceZero = bondPricer.bondPriceForHazardRate(bond, yc, 0., PriceType.DIRTY);
    final double hrZero = bondPricer.getHazardRate(bond, yc, priceZero, PriceType.DIRTY);
    assertEquals(0., hrZero, tol);

    /*
     * Exception thrown
     */
    try {
      bondPricer.getHazardRate(bond, yc, priceZero * 2., PriceType.CLEAN);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      bondPricer.getHazardRate(bond, yc, bond.getRecoveryRate() * 0.5, PriceType.DIRTY);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("The dirty price of " + bond.getRecoveryRate() * 0.5 + " give, is less than the bond's recovery rate of " + bond.getRecoveryRate() + ". Please check inputs", e.getMessage());
    }
    try {
      bondPricer.getHazardRate(bond, yc, -cleanPrice, PriceType.CLEAN);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("Bond price must be positive", e.getMessage());
    }
  }

  /**
   * 
   */
  public void exceptionalBranchTest() {
    final double tol = 1.e-12;
    final AnalyticBondPricer bondPricer = new AnalyticBondPricer();

    final LocalDate tradeDate = LocalDate.of(2014, 2, 13);
    final LocalDate startDate = LocalDate.of(2013, 12, 20);
    final LocalDate endDate = LocalDate.of(2014, 12, 20);
    final double coupon = 0.11;
    double rr = 0.3;
    final Period couponPrd = Period.ofMonths(6);
    final StubType stubTp = StubType.FRONTSHORT;
    final BusinessDayConvention bdConv = MOD_FOLLOWING;
    final Calendar cal = DEFAULT_CALENDAR;
    final boolean ProtStart = true;
    final ISDAPremiumLegSchedule schedule = new ISDAPremiumLegSchedule(startDate, endDate, couponPrd, stubTp, bdConv, cal, ProtStart);
    final BondAnalytic bond = new BondAnalytic(tradeDate, coupon, schedule, rr, ACT360);
    final double bondPaymentTimeLast = bond.getPaymentTime(bond.getnPayments() - 1);

    final double hr = 0.11;
    final ISDACompliantYieldCurve yc1 = new ISDACompliantYieldCurve(new double[] {0.3 * bondPaymentTimeLast, 0.7 * bondPaymentTimeLast,
        bondPaymentTimeLast, 1.2 * bondPaymentTimeLast, 2. * bondPaymentTimeLast }, new double[] {-hr, 0.11, 0.044, 0.1, 0.12 });
    final ISDACompliantYieldCurve yc2 = new ISDACompliantYieldCurve(new double[] {1.1 * bondPaymentTimeLast, 1.2 * bondPaymentTimeLast,
        1.3 * bondPaymentTimeLast, 1.5 * bondPaymentTimeLast, 2.1 * bondPaymentTimeLast }, new double[] {0.1, 0.11, 0.08, 0.12, 0.12 });
    final ISDACompliantYieldCurve yc3 = new ISDACompliantYieldCurve(new double[] {bondPaymentTimeLast, 1.2 * bondPaymentTimeLast,
        1.3 * bondPaymentTimeLast, 1.5 * bondPaymentTimeLast, 2.1 * bondPaymentTimeLast }, new double[] {0.12, 0.11, 0.08, 0.12, 0.12 });
    final ISDACompliantYieldCurve[] ycArr = new ISDACompliantYieldCurve[] {yc1, yc2, yc3 };
    final int nyc = ycArr.length;

    for (int i = 0; i < nyc; ++i) {
      final double bondPrice = bondPricer.bondPriceForHazardRate(bond, ycArr[i], hr, PriceType.CLEAN);
      final double impliedHr = bondPricer.getHazardRate(bond, ycArr[i], bondPrice, PriceType.CLEAN);
      assertEquals(hr, impliedHr, tol);
    }
  }

  private double defaultFreeBondCleanPrice(final BondAnalytic bond, final ISDACompliantYieldCurve yieldCurve) {
    double pv = -bond.getAccruedInterest();
    final int n = bond.getnPayments();
    for (int i = 0; i < n; i++) {
      pv += bond.getPaymentAmount(i) * yieldCurve.getDiscountFactor(bond.getPaymentTime(i));
    }
    return pv;
  }

}
