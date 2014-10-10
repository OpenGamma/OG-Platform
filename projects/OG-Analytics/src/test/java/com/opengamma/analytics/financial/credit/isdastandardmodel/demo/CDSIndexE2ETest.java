/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel.demo;

import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.CDS_TENORS;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.CDX_NA_HY_20140213_PAR_SPREADS;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.CDX_NA_HY_20140213_PRICES;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.CDX_NA_HY_20140213_RECOVERY_RATES;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.CDX_NA_HY_21_COUPON;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.CDX_NA_HY_21_RECOVERY_RATE;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.INDEX_TENORS;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.buildCreditCurves;
import static com.opengamma.analytics.financial.credit.options.YieldCurveProvider.ISDA_USD_20140213;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.index.CDSIndexCalculator;
import com.opengamma.analytics.financial.credit.index.IntrinsicIndexDataBundle;
import com.opengamma.analytics.financial.credit.index.PortfolioSwapAdjustment;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDABaseTest;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PointsUpFront;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PriceType;

/**
 * 
 */
public class CDSIndexE2ETest extends ISDABaseTest {
  private static final double NOTIONAL = 1e8;

  private static final LocalDate TRADE_DATE = LocalDate.of(2014, 2, 13);

  private static final LocalDate EXPIRY = LocalDate.of(2014, 3, 19);
  private static final LocalDate EXERCISE_SETTLE = LocalDate.of(2014, 3, 24);
  private static final LocalDate MATURITY = LocalDate.of(2018, 12, 20);

  private static final double INDEX_COUPON = CDX_NA_HY_21_COUPON;
  private static final double INDEX_RECOVERY = CDX_NA_HY_21_RECOVERY_RATE;

  // private static final double EXP_FWD_ANNUITY = 4.83644025;
  private static final Period TENOR = Period.ofYears(5);

  private static final ISDACompliantCreditCurve[] CREDIT_CURVES = buildCreditCurves(TRADE_DATE,
      CDX_NA_HY_20140213_PAR_SPREADS, CDX_NA_HY_20140213_RECOVERY_RATES, CDS_TENORS, ISDA_USD_20140213);  //decompose this !!!!
  private static final double[] RECOVERY_RATES = CDX_NA_HY_20140213_RECOVERY_RATES;
  private static IntrinsicIndexDataBundle INTRINSIC_DATA = new IntrinsicIndexDataBundle(CREDIT_CURVES, RECOVERY_RATES);
  private static ISDACompliantYieldCurve YIELD_CURVE = ISDA_USD_20140213; //desompose this !!!!
  private static double[] PRICES = CDX_NA_HY_20140213_PRICES;
  private static int INDEX_SIZE = RECOVERY_RATES.length;

  private static final PointsUpFront[] PILLAR_PUF;
  private static final CDSAnalyticFactory FACTORY = new CDSAnalyticFactory(INDEX_RECOVERY);
  //  private static final CDSAnalytic FWD_START_CDX = FACTORY.makeForwardStartingCDS(TRADE_DATE, EXPIRY, MATURITY);
  private static final CDSAnalytic CDX = FACTORY.makeCDX(TRADE_DATE, TENOR);
  private static final CDSAnalytic[] PILLAR_CDX = FACTORY.makeCDX(TRADE_DATE, INDEX_TENORS);

  private static final PortfolioSwapAdjustment PSA = new PortfolioSwapAdjustment();
  private static final CDSIndexCalculator INDEX_CAL = new CDSIndexCalculator();

  //  private static final boolean PRINT = false;

  static {
    final int n = PRICES.length;
    PILLAR_PUF = new PointsUpFront[n];
    for (int i = 0; i < n; i++) {
      PILLAR_PUF[i] = new PointsUpFront(INDEX_COUPON, 1 - PRICES[i]);
    }

    //    if (PRINT) {
    //      System.out.println("CDXNAHYTest - set PRINT to false before push");
    //    }
  }

  @Test(enabled = false)
  public void forwardValueTest() {
    final int n = PILLAR_PUF.length;
    final double[] indexPUF = new double[n];
    for (int i = 0; i < n; i++) {
      indexPUF[i] = PILLAR_PUF[i].getPointsUpFront();
    }
    final IntrinsicIndexDataBundle adjCurves = PSA.adjustCurves(indexPUF, PILLAR_CDX, INDEX_COUPON, YIELD_CURVE,
        INTRINSIC_DATA);
    double cleanPrice = INDEX_CAL.indexPV(CDX, INDEX_COUPON, YIELD_CURVE, adjCurves) * NOTIONAL;
    double dirtyPrice = INDEX_CAL.indexPV(CDX, INDEX_COUPON, YIELD_CURVE, adjCurves, PriceType.DIRTY) * NOTIONAL;

    System.out.println(cleanPrice);
    System.out.println(dirtyPrice);
    //
    //    final double tE = ACT365F.getDayCountFraction(TRADE_DATE, EXPIRY);
    //    final double tES = ACT365F.getDayCountFraction(TRADE_DATE, EXERCISE_SETTLE);
    //    final double expFwdPrice1 = -7149840.399010934 / NOTIONAL;
    //    final double expFwdPrice2 = -7121885.71427097 / NOTIONAL;
    //    final double expFwdPrice3 = -7150407.604015437 / NOTIONAL;
    //    final double expFwdSpread1 = 333.22161805754155 * ONE_BP;
    //    final double expFwdSpread2 = 333.5071399772611 * ONE_BP;
    //    final double expFwdSpread3 = 333.06693990756 * ONE_BP;
    //
    //    //build (pseudo) index credit curve from PUF
    //    final ISDACompliantCreditCurve cc = CREDIT_CURVE_BUILDER.calibrateCreditCurve(PILLAR_CDX, PILLAR_PUF, YIELD_CURVE);
    //
    //    final double q = cc.getSurvivalProbability(tE);
    //    final double df = YIELD_CURVE.getDiscountFactor(tES);
    //
    //    final double fwdProt = PRICER.protectionLeg(FWD_START_CDX, YIELD_CURVE, cc, 0) + df * (1 - INDEX_RECOVERY) *
    //        (1 - q);
    //    final double fwdAnn = PRICER.annuity(FWD_START_CDX, YIELD_CURVE, cc, PriceType.CLEAN, 0);
    //    final double fwdSpread = fwdProt / fwdAnn;
    //
    //    final double fwdSpread2 = INDEX_CAL.defaultAdjustedForwardSpread(FWD_START_CDX, tE, YIELD_CURVE, cc);
    //    assertEquals(fwdSpread, fwdSpread2, 1e-15);
    //
    //    final double fwdIndexVal = ((fwdProt - INDEX_COUPON * fwdAnn) / df);
    //    final double fwdIndexVal2 = INDEX_CAL.defaultAdjustedForwardIndexValue(FWD_START_CDX, tE, YIELD_CURVE,
    //        INDEX_COUPON, cc);
    //    assertEquals(fwdIndexVal, fwdIndexVal2, 1e-15);
    //
    //    if (PRINT) {
    //      System.out.println("df: " + df);
    //      System.out.println("Fwd Spread: " + fwdSpread * TEN_THOUSAND);
    //      System.out.println("Fwd Index val: " + NOTIONAL * fwdIndexVal);
    //    }
    //
    //    final double fwdSpreadUnAdj = INDEX_CAL
    //        .defaultAdjustedForwardSpread(FWD_START_CDX, tE, YIELD_CURVE, INTRINSIC_DATA);
    //    final double fwdIndexValUnAdj = INDEX_CAL.defaultAdjustedForwardIndexValue(FWD_START_CDX, tE, YIELD_CURVE,
    //        INDEX_COUPON, INTRINSIC_DATA);
    //
    //    final double fwdSpreadAdj = INDEX_CAL.defaultAdjustedForwardSpread(FWD_START_CDX, tE, YIELD_CURVE, adjCurves);
    //    final double fwdIndexValAdj = INDEX_CAL.defaultAdjustedForwardIndexValue(FWD_START_CDX, tE, YIELD_CURVE,
    //        INDEX_COUPON, adjCurves);
    //
    //    if (PRINT) {//intrinsic 
    //      System.out.println("Intrinsic fwd spread before and after adjustment:\t" + fwdSpreadUnAdj * TEN_THOUSAND + "\t" +
    //          fwdSpreadAdj * TEN_THOUSAND);
    //      System.out.println("Intrinsic fwd value before and after adjustment:\t" + fwdIndexValUnAdj * NOTIONAL + "\t" +
    //          fwdIndexValAdj * NOTIONAL);
    //    }
    //
    //    assertEquals("Regression test for index curve ATM Fwd", expFwdPrice1, fwdIndexVal2, 1e-15);
    //    assertEquals("Regression test for intrinic ATM Fwd", expFwdPrice2, fwdIndexValUnAdj, 1e-15);
    //    assertEquals("Regression test for adjusted intrinic ATM Fwd", expFwdPrice3, fwdIndexValAdj, 1e-15);
    //    assertEquals("Regression test for index curve ATM Fwd spread", expFwdSpread1, fwdSpread2, 1e-15);
    //    assertEquals("Regression test for intrinic ATM Fwd spread", expFwdSpread2, fwdSpreadUnAdj, 1e-15);
    //    assertEquals("Regression test for adjusted intrinic ATM Fwd spread", expFwdSpread3, fwdSpreadAdj, 1e-15);
  }
}
