/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.options.YieldCurveProvider;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;

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

  private double defaultFreeBondCleanPrice(final BondAnalytic bond, final ISDACompliantYieldCurve yieldCurve) {
    double pv = -bond.getAccruedInterest();
    final int n = bond.getnPayments();
    for (int i = 0; i < n; i++) {
      pv += bond.getPaymentAmount(i) * yieldCurve.getDiscountFactor(bond.getPaymentTime(i));
    }
    return pv;
  }

}
