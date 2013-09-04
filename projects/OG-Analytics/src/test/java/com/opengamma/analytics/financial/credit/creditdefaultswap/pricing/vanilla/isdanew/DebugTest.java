/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.IMMDateLogic.getIMMDateSet;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.IMMDateLogic.getNextIMMDate;
import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantCreditCurveBuilder.ArbitrageHandling;

/**
 * 
 */
public class DebugTest extends ISDABaseTest {

  private static final LocalDate TODAY = LocalDate.of(2011, Month.JUNE, 13);
  private static final LocalDate NEXT_IMM = getNextIMMDate(TODAY);

  private static final LocalDate TRADE_DATE = LocalDate.of(2011, Month.JUNE, 14);
  private static final LocalDate EFFECTIVE_DATE = TRADE_DATE.plusDays(1); // AKA stepin date
  private static final LocalDate CASH_SETTLE_DATE = addWorkDays(TRADE_DATE, 3, DEFAULT_CALENDAR); // AKA valuation date
  private static final LocalDate STARTDATE = LocalDate.of(2011, Month.MARCH, 20);

  private static final Period[] TENORS = new Period[] {Period.ofMonths(6), Period.ofYears(1), Period.ofYears(3), Period.ofYears(5), Period.ofYears(7), Period.ofYears(10) };
  //  private static final LocalDate NEXT_IMM = getNextIMMDate(EFFECTIVE_DATE);
  private static final LocalDate[] PILLAR_DATES = getIMMDateSet(NEXT_IMM, TENORS);
  private static final LocalDate[] MATURITIES = getIMMDateSet(NEXT_IMM, 41);

  //yield curve
  private static final LocalDate SPOT_DATE = LocalDate.of(2011, Month.JUNE, 15);
  private static final String[] YIELD_CURVE_POINTS = new String[] {"1M", "2M", "3M", "6M", "9M", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y" };
  private static final String[] YIELD_CURVE_INSTRUMENTS = new String[] {"M", "M", "M", "M", "M", "M", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" };
  private static final double[] YIELD_CURVE_RATES = new double[] {0.00445, 0.009488, 0.012337, 0.017762, 0.01935, 0.020838, 0.01652, 0.02018, 0.023033, 0.02525, 0.02696, 0.02825, 0.02931, 0.03017,
    0.03092, 0.0316, 0.03231, 0.03367, 0.03419, 0.03411, 0.03412 };
  private static final ISDACompliantYieldCurve YIELD_CURVE = makeYieldCurve(TRADE_DATE, SPOT_DATE, YIELD_CURVE_POINTS, YIELD_CURVE_INSTRUMENTS, YIELD_CURVE_RATES, ACT360, D30360, Period.ofYears(1));

  private static final double COUPON = 0.01;
  private static final double[] SPREADS = new double[] {0.007926718, 0.007926718, 0.012239372, 0.016978579, 0.019270856, 0.02086048 };

  @Test(enabled = false)
  public void test() {
    final ISDACompliantCreditCurveBuilder curveBuilder = new FastCreditCurveBuilder(true, ArbitrageHandling.ZeroHazardRate);
    final AnalyticCDSPricer pricer = new AnalyticCDSPricer(true);

    final int nPillars = PILLAR_DATES.length;
    final CDSAnalytic[] pillarCDSS = new CDSAnalytic[nPillars];
    for (int i = 0; i < nPillars; i++) {
      pillarCDSS[i] = new CDSAnalytic(TRADE_DATE, EFFECTIVE_DATE, CASH_SETTLE_DATE, STARTDATE, PILLAR_DATES[i], PAY_ACC_ON_DEFAULT, PAYMENT_INTERVAL, STUB, PROCTECTION_START, RECOVERY_RATE);
    }

    final ISDACompliantCreditCurve creditCurve = curveBuilder.calibrateCreditCurve(pillarCDSS, SPREADS, YIELD_CURVE);

    final int nMat = MATURITIES.length;
    for (int i = 0; i < nMat; i++) {
      final CDSAnalytic cds = new CDSAnalytic(TRADE_DATE, EFFECTIVE_DATE, CASH_SETTLE_DATE, STARTDATE, MATURITIES[i], PAY_ACC_ON_DEFAULT, PAYMENT_INTERVAL, STUB, PROCTECTION_START, RECOVERY_RATE);
      final double dPV = pricer.pv(cds, YIELD_CURVE, creditCurve, COUPON, PriceType.DIRTY);
      final double proLeg = pricer.protectionLeg(cds, YIELD_CURVE, creditCurve);

      System.out.println(MATURITIES[i] + "\t" + dPV + "\t" + proLeg);
      //   assertEquals(MATURITIES[i].toString(), EXPECTED_UPFRONT_CHARGE[i], dPV, 1e-15);
    }

  }

  @Test
  //(enabled = false)
  public void funcTest() {
    final CDSAnalyticFactory factory = new CDSAnalyticFactory(1.0);
    final CDSAnalytic cds = factory.makeIMMCDS(TRADE_DATE, Period.ofYears(5));
    for (int i = 0; i < 100; i++) {
      final double lambda = 0.8 * i / 100.;
      final ISDACompliantCreditCurve cc = new ISDACompliantCreditCurve(5.0, lambda);
      final double price = PRICER_CORRECT.pv(cds, YIELD_CURVE, cc, 0.05);
      System.out.println(lambda + "\t" + price);
    }

  }

}
