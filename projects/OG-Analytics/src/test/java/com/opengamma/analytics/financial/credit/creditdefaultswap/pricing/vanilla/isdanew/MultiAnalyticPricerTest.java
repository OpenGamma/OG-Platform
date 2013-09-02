/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.IMMDateLogic.getNextIMMDate;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.IMMDateLogic.getPrevIMMDate;
import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class MultiAnalyticPricerTest extends ISDABaseTest {

  private static final MultiAnalyticCDSPricer MULTI_PRICER = new MultiAnalyticCDSPricer(false);
  private static final MultiAnalyticCDSPricer MULTI_PRICER_CORRECT = new MultiAnalyticCDSPricer(true);
  private static final CDSAnalyticFactory FACTORY = new CDSAnalyticFactory();

  private static final ISDACompliantYieldCurve YIELD_CURVE;
  private static final ISDACompliantCreditCurve CREDIT_CURVE;

  static {
    final double[] yieldCurveNodes = new double[] {1 / 365., 1 / 52., 1 / 12., 1 / 4., 1 / 2., 1., 2., 3., 4., 5., 7., 10, 15, 20, 30 };
    final double[] zeroRates = new double[] {0.01, 0.011, 0.013, 0.015, 0.02, 0.03, 0.035, 0.04, 0.04, 0.06, 0.06, 0.057, 0.055, 0.05, 0.05 };
    YIELD_CURVE = new ISDACompliantYieldCurve(yieldCurveNodes, zeroRates);
    final double[] creditCurveNodes = new double[] {1 / 2., 1, 2, 3, 5, 7, 10 };
    final double[] zeroHazardRates = new double[] {0.0015, 0.002, 0.0023, 0.0025, 0.0024, 0.0023, 0.002 };
    CREDIT_CURVE = new ISDACompliantCreditCurve(creditCurveNodes, zeroHazardRates);
  }

  @Test
  public void singleCDSTest() {
    final LocalDate tradeDate = LocalDate.of(2013, Month.AUGUST, 30);
    final LocalDate effectiveDate = FOLLOWING.adjustDate(DEFAULT_CALENDAR, getPrevIMMDate(tradeDate));
    final LocalDate stepinDate = tradeDate.plusDays(1);
    final LocalDate valueDate = addWorkDays(tradeDate, 3, DEFAULT_CALENDAR);
    final LocalDate nextIMM = getNextIMMDate(tradeDate);
    final int matIndex = 4;
    LocalDate temp = nextIMM;
    for (int i = 0; i < matIndex; i++) {
      temp = temp.plus(PAYMENT_INTERVAL);
    }
    final LocalDate maturity = temp;
    final double coupon = 0.0075;
    final Tenor paymentInt = Tenor.of(PAYMENT_INTERVAL);

    final CDSAnalytic cdsS = FACTORY.makeCDS(tradeDate, effectiveDate, maturity);
    final MultiCDSAnalytic cdsM = new MultiCDSAnalytic(tradeDate, stepinDate, valueDate, effectiveDate, nextIMM, new int[] {matIndex }, new double[] {coupon }, PAY_ACC_ON_DEFAULT, paymentInt, STUB,
        PROCTECTION_START, RECOVERY_RATE, FOLLOWING, DEFAULT_CALENDAR, ACT360, ACT365);

    final double proLegS = PRICER.protectionLeg(cdsS, YIELD_CURVE, CREDIT_CURVE);
    final double proLegM = MULTI_PRICER.protectionLeg(cdsM, YIELD_CURVE, CREDIT_CURVE)[0];
    //    System.out.println("pro leg: " + proLegS + "\t" + proLegM);
    final double rpv01S = PRICER.pvPremiumLegPerUnitSpread(cdsS, YIELD_CURVE, CREDIT_CURVE, PriceType.CLEAN);
    final double rpv01M = MULTI_PRICER.pvPremiumLegPerUnitSpread(cdsM, YIELD_CURVE, CREDIT_CURVE, PriceType.CLEAN)[0];
    //  System.out.println("rpv01: " + rpv01S + "\t" + rpv01M);

    //These are identical calculations, so the match should be exact 
    assertEquals("proLeg", proLegS, proLegM, 0);
    assertEquals("RPV01", rpv01S, rpv01M, 0);
  }

  @Test
  public void multiCDSTest() {
    final LocalDate tradeDate = LocalDate.of(2013, Month.AUGUST, 30);
    final LocalDate effectiveDate = FOLLOWING.adjustDate(DEFAULT_CALENDAR, getPrevIMMDate(tradeDate));
    final LocalDate stepinDate = tradeDate.plusDays(1);
    final LocalDate valueDate = addWorkDays(tradeDate, 3, DEFAULT_CALENDAR);
    final LocalDate nextIMM = getNextIMMDate(tradeDate);
    final int[] matIndex = new int[] {1, 2, 4, 8, 12, 20, 28 };
    final int nMat = matIndex.length;

    final LocalDate[] maturities = new LocalDate[nMat];

    LocalDate tMat = nextIMM;
    for (int i = 0; i < nMat; i++) {
      final int steps = i == 0 ? matIndex[0] : matIndex[i] - matIndex[i - 1];
      for (int j = 0; j < steps; j++) {
        tMat = tMat.plus(PAYMENT_INTERVAL);
      }
      maturities[i] = tMat;
    }

    final double[] coupons = new double[] {0.0075, 0.008, 0.01, 0.01, 0.011, 0.01, 0.009 };
    final Tenor paymentInt = Tenor.of(PAYMENT_INTERVAL);

    final CDSAnalytic[] cdsS = FACTORY.makeCDS(tradeDate, effectiveDate, maturities);
    final MultiCDSAnalytic cdsM = new MultiCDSAnalytic(tradeDate, stepinDate, valueDate, effectiveDate, nextIMM, matIndex, coupons, PAY_ACC_ON_DEFAULT, paymentInt, STUB, PROCTECTION_START,
        RECOVERY_RATE, FOLLOWING, DEFAULT_CALENDAR, ACT360, ACT365);

    final double[] pvM = MULTI_PRICER.pv(cdsM, YIELD_CURVE, CREDIT_CURVE);
    //    final double[] proLegM = MULTI_PRICER.protectionLeg(cdsM, YIELD_CURVE, CREDIT_CURVE);
    //    final double[] rpv01M = MULTI_PRICER.pvPremiumLegPerUnitSpread(cdsM, YIELD_CURVE, CREDIT_CURVE, PriceType.CLEAN);

    final double[] pvS = new double[nMat];
    for (int i = 0; i < nMat; i++) {
      pvS[i] = PRICER.pv(cdsS[i], YIELD_CURVE, CREDIT_CURVE, coupons[i]);
    }

    //These take different paths, so the match will not be exact 
    for (int i = 0; i < nMat; i++) {
      assertEquals("pv " + i, pvS[i], pvM[i], 1e-16);
    }

    //check to correct integral prices 
    final double[] pvMC = MULTI_PRICER_CORRECT.pv(cdsM, YIELD_CURVE, CREDIT_CURVE);
    final double[] pvSC = new double[nMat];
    for (int i = 0; i < nMat; i++) {
      pvSC[i] = PRICER_CORRECT.pv(cdsS[i], YIELD_CURVE, CREDIT_CURVE, coupons[i]);
    }

    //These take different paths, so the match will not be exact 
    for (int i = 0; i < nMat; i++) {
      assertEquals("pv " + i, pvSC[i], pvMC[i], 1e-16);
    }
  }

  @Test(enabled = false)
  public void speedTest() {
    final int warmups = 200;
    final int hotspot = 1000;

    final LocalDate tradeDate = LocalDate.of(2013, Month.AUGUST, 30);
    final LocalDate effectiveDate = FOLLOWING.adjustDate(DEFAULT_CALENDAR, getPrevIMMDate(tradeDate));
    final LocalDate stepinDate = tradeDate.plusDays(1);
    final LocalDate valueDate = addWorkDays(tradeDate, 3, DEFAULT_CALENDAR);
    final LocalDate nextIMM = getNextIMMDate(tradeDate);
    final int[] matIndex = new int[41];
    final int nMat = matIndex.length;
    final double[] coupons = new double[nMat];
    for (int i = 0; i < nMat; i++) {
      matIndex[i] = i;
      coupons[i] = 0.001 + i / 4000.;
    }

    final LocalDate[] maturities = new LocalDate[nMat];

    LocalDate tMat = nextIMM;
    for (int i = 0; i < nMat; i++) {
      final int steps = i == 0 ? matIndex[0] : matIndex[i] - matIndex[i - 1];
      for (int j = 0; j < steps; j++) {
        tMat = tMat.plus(PAYMENT_INTERVAL);
      }
      maturities[i] = tMat;
    }
    final Tenor paymentInt = Tenor.of(PAYMENT_INTERVAL);

    final CDSAnalytic[] cdsS = FACTORY.makeCDS(tradeDate, effectiveDate, maturities);
    final MultiCDSAnalytic cdsM = new MultiCDSAnalytic(tradeDate, stepinDate, valueDate, effectiveDate, nextIMM, matIndex, coupons, PAY_ACC_ON_DEFAULT, paymentInt, STUB, PROCTECTION_START,
        RECOVERY_RATE, FOLLOWING, DEFAULT_CALENDAR, ACT360, ACT365);

    final double[] pvSC = new double[nMat];
    double[] pvMC = null;

    for (int w = 0; w < warmups; w++) {
      for (int i = 0; i < nMat; i++) {
        pvSC[i] = PRICER_CORRECT.pv(cdsS[i], YIELD_CURVE, CREDIT_CURVE, coupons[i]);
      }
      pvMC = MULTI_PRICER_CORRECT.pv(cdsM, YIELD_CURVE, CREDIT_CURVE);
    }

    //These take different paths, so the match will not be exact 
    for (int i = 0; i < nMat; i++) {
      assertEquals("pv " + i, pvSC[i], pvMC[i], 1e-16);
    }

    long time = System.nanoTime();
    for (int h = 0; h < hotspot; h++) {
      for (int i = 0; i < nMat; i++) {
        pvSC[i] = PRICER_CORRECT.pv(cdsS[i], YIELD_CURVE, CREDIT_CURVE, coupons[i]);
      }
    }
    long nextTime = System.nanoTime();
    System.out.println("Time for " + hotspot + " single CDS prices " + (nextTime - time) / 1e6 + "ms");
    time = nextTime;
    for (int h = 0; h < hotspot; h++) {
      pvMC = MULTI_PRICER_CORRECT.pv(cdsM, YIELD_CURVE, CREDIT_CURVE);
    }
    nextTime = System.nanoTime();
    System.out.println("Time for " + hotspot + " multi CDS prices " + (nextTime - time) / 1e6 + "ms");
  }

}
