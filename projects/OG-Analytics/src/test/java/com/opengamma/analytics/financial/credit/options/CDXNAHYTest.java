/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.options;

import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.CDX_NA_HY_20140213_PRICES;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.CDX_NA_HY_20140213_RECOVERY_RATES;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.CDX_NA_HY_21_COUPON;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.CDX_NA_HY_21_RECOVERY_RATE;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.INDEX_TENORS;
import static com.opengamma.analytics.financial.credit.options.CDSIndexPrvider.getCDX_NA_HY_20140213_CreditCurves;
import static com.opengamma.analytics.financial.credit.options.YieldCurveProvider.ISDA_USD_20140213;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.index.CDSIndexCalculator;
import com.opengamma.analytics.financial.credit.index.PortfolioSwapAdjustment;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDABaseTest;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.MarketQuoteConverter;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PointsUpFront;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PriceType;

/**
 * 
 */
public class CDXNAHYTest extends ISDABaseTest {
  private static final double NOTIONAL = 1e8;

  private static final LocalDate ACC_START = LocalDate.of(2013, 12, 20);
  private static final LocalDate TRADE_DATE = LocalDate.of(2014, 2, 13);
  private static final LocalDate CASH_SETTLEMENT_DATE = LocalDate.of(2014, 2, 19);
  private static final LocalDate EXPIRY = LocalDate.of(2014, 3, 19);
  private static final LocalDate EXERCISE_SETTLE = LocalDate.of(2014, 3, 24);
  private static final LocalDate MATURITY = LocalDate.of(2018, 12, 20);
  private static final double PRICE = 107.62 * ONE_PC;

  private static final double INDEX_COUPON = CDX_NA_HY_21_COUPON;
  private static final double INDEX_RECOVERY = CDX_NA_HY_21_RECOVERY_RATE;

  // private static final double EXP_FWD_ANNUITY = 4.83644025;
  private static final double BBG_ATM_FWD = 107.1444434 * ONE_PC;
  private static final Period TENOR = Period.ofYears(5);

  private static final ISDACompliantCreditCurve[] CREDIT_CURVES = getCDX_NA_HY_20140213_CreditCurves();
  private static final double[] RECOVERY_RATES = CDX_NA_HY_20140213_RECOVERY_RATES;
  private static ISDACompliantYieldCurve YIELD_CURVE = ISDA_USD_20140213;
  private static double[] PRICES = CDX_NA_HY_20140213_PRICES;
  private static int INDEX_SIZE = RECOVERY_RATES.length;

  private static final PointsUpFront[] PILLAR_PUF;

  private static final MarketQuoteConverter CONVERTER = new MarketQuoteConverter();
  private static final CDSAnalyticFactory FACTORY = new CDSAnalyticFactory(INDEX_RECOVERY);
  private static final CDSAnalytic SPOT_CDX = FACTORY.makeCDX(TRADE_DATE, TENOR);
  private static final CDSAnalytic FWD_START_CDX = FACTORY.makeForwardStartingCDS(TRADE_DATE, EXPIRY, MATURITY);
  private static final CDSAnalytic FWD_CDX = FACTORY.makeCDX(EXPIRY, TENOR);
  private static final CDSAnalytic[] PILLAR_CDX = FACTORY.makeCDX(TRADE_DATE, INDEX_TENORS);

  private static final double[] STRIKES = new double[] {103, 104, 105, 106, 107.1444434, 108, 109, 110, 111 };
  private static final double[] CALLPRICE = new double[] {1050.57, 7281.74, 38630.22, 154451.94, 529900.91, 1053705.97, 1894442.5, 2859098.57, 3855162.16 };
  private static final double[] PUTPRICE = new double[] {4144889.03, 3151266.17, 2182760.61, 1298728.3, 529900.91, 198274.25, 39156.75, 3958.78, 168.34 };

  private static final PortfolioSwapAdjustment PSA = new PortfolioSwapAdjustment();
  private static final CDSIndexCalculator INDEX_CAL = new CDSIndexCalculator();

  private static final boolean PRINT = true;

  static {
    final int n = PRICES.length;
    PILLAR_PUF = new PointsUpFront[n];
    for (int i = 0; i < n; i++) {
      PILLAR_PUF[i] = new PointsUpFront(INDEX_COUPON, 1 - PRICES[i]);
    }

    if (PRINT) {
      System.out.println("CDXNAHYTest - set PRINT to false before push");
    }
  }

  /**
   * This computes the (default adjusted) forward spread and index value using a pseudo index credit curve and adjusted intrinsic curves 
   */
  @Test
  public void forwardValueTest() {
    final double tE = ACT365F.getDayCountFraction(TRADE_DATE, EXPIRY);
    final double tES = ACT365F.getDayCountFraction(TRADE_DATE, EXERCISE_SETTLE);
    final double expFwdPrice1 = -7149840.399010934 / NOTIONAL;
    final double expFwdPrice2 = -7121885.71427097 / NOTIONAL;
    final double expFwdPrice3 = -7150407.604015437 / NOTIONAL;
    final double expFwdSpread1 = 333.22161805754155 * ONE_BP;
    final double expFwdSpread2 = 333.5071399772611 * ONE_BP;
    final double expFwdSpread3 = 333.06693990756 * ONE_BP;

    //build (pseudo) index credit curve from PUF
    final ISDACompliantCreditCurve cc = CREDIT_CURVE_BUILDER.calibrateCreditCurve(PILLAR_CDX, PILLAR_PUF, YIELD_CURVE);

    final double q = cc.getSurvivalProbability(tE);
    final double df = YIELD_CURVE.getDiscountFactor(tES);

    final double fwdProt = PRICER.protectionLeg(FWD_START_CDX, YIELD_CURVE, cc, 0) + df * (1 - INDEX_RECOVERY) * (1 - q);
    final double fwdAnn = PRICER.annuity(FWD_START_CDX, YIELD_CURVE, cc, PriceType.CLEAN, 0);
    final double fwdSpread = fwdProt / fwdAnn;

    final double fwdSpread2 = INDEX_CAL.forwardSpread(FWD_START_CDX, tE, 1, YIELD_CURVE, new ISDACompliantCreditCurve[] {cc }, new double[] {INDEX_RECOVERY }, 0);
    assertEquals(fwdSpread, fwdSpread2, 1e-15);

    final double fwdIndexVal = ((fwdProt - INDEX_COUPON * fwdAnn) / df);
    final double fwdIndexVal2 = INDEX_CAL.defaultAdjustedForwardIndexValue(FWD_START_CDX, tE, 1, YIELD_CURVE, INDEX_COUPON, new ISDACompliantCreditCurve[] {cc }, new double[] {INDEX_RECOVERY });
    assertEquals(fwdIndexVal, fwdIndexVal2, 1e-15);

    if (PRINT) {
      System.out.println("df: " + df);
      System.out.println("Fwd Spread: " + fwdSpread * TEN_THOUSAND);
      System.out.println("Fwd Index val: " + NOTIONAL * fwdIndexVal);
    }

    final double fwdSpreadUnAdj = INDEX_CAL.forwardSpread(FWD_START_CDX, tE, INDEX_SIZE, YIELD_CURVE, CREDIT_CURVES, RECOVERY_RATES, 0);
    final double fwdIndexValUnAdj = INDEX_CAL.defaultAdjustedForwardIndexValue(FWD_START_CDX, tE, INDEX_SIZE, YIELD_CURVE, INDEX_COUPON, CREDIT_CURVES, RECOVERY_RATES);

    final int n = PILLAR_PUF.length;
    final double[] indexPUF = new double[n];
    for (int i = 0; i < n; i++) {
      indexPUF[i] = PILLAR_PUF[i].getPointsUpFront();
    }
    final ISDACompliantCreditCurve[] adjCurves = PSA.adjustCurves(PILLAR_CDX, INDEX_COUPON, YIELD_CURVE, CREDIT_CURVES, RECOVERY_RATES, indexPUF);
    final double fwdSpreadAdj = INDEX_CAL.forwardSpread(FWD_START_CDX, tE, INDEX_SIZE, YIELD_CURVE, adjCurves, RECOVERY_RATES, 0);
    final double fwdIndexValAdj = INDEX_CAL.defaultAdjustedForwardIndexValue(FWD_START_CDX, tE, INDEX_SIZE, YIELD_CURVE, INDEX_COUPON, adjCurves, RECOVERY_RATES);

    if (PRINT) {//intrinsic 
      System.out.println("Intrinsic fwd spread before and after adjustment:\t" + fwdSpreadUnAdj * TEN_THOUSAND + "\t" + fwdSpreadAdj * TEN_THOUSAND);
      System.out.println("Intrinsic fwd value before and after adjustment:\t" + fwdIndexValUnAdj * NOTIONAL + "\t" + fwdIndexValAdj * NOTIONAL);
    }

    assertEquals("Regression test for index curve ATM Fwd", expFwdPrice1, fwdIndexVal2, 1e-15);
    assertEquals("Regression test for intrinic ATM Fwd", expFwdPrice2, fwdIndexValUnAdj, 1e-15);
    assertEquals("Regression test for adjusted intrinic ATM Fwd", expFwdPrice3, fwdIndexValAdj, 1e-15);
    assertEquals("Regression test for index curve ATM Fwd spread", expFwdSpread1, fwdSpread2, 1e-15);
    assertEquals("Regression test for intrinic ATM Fwd spread", expFwdSpread2, fwdSpreadUnAdj, 1e-15);
    assertEquals("Regression test for adjusted intrinic ATM Fwd spread", expFwdSpread3, fwdSpreadAdj, 1e-15);
  }

  @Test
  void forwardValueWithAccrualTest() {
    final LocalDate expiry = LocalDate.of(2014, 3, 18);
    final CDSAnalytic fwdStartCDS = FACTORY.makeForwardStartingCDS(TRADE_DATE, expiry, MATURITY);
    //  assertEquals(89, fwdStartCDS.getAccuredDays());

    final ISDACompliantCreditCurve cc = CREDIT_CURVE_BUILDER.calibrateCreditCurve(PILLAR_CDX, PILLAR_PUF, YIELD_CURVE);
    final double tE = ACT365F.getDayCountFraction(TRADE_DATE, expiry);
    final double tES = ACT365F.getDayCountFraction(TRADE_DATE, EXERCISE_SETTLE.minusDays(1));
    final double fwdIndexVal = INDEX_CAL.defaultAdjustedForwardIndexValue(fwdStartCDS, tE, YIELD_CURVE, INDEX_COUPON, cc);
    System.out.println(fwdIndexVal * NOTIONAL);

    final double defSet = INDEX_CAL.expectedDefaultSettlementValue(tE, cc, fwdStartCDS.getLGD());
    final double fwdIndexVal2 = defSet + PRICER.pv(fwdStartCDS, YIELD_CURVE, cc, INDEX_COUPON);
    System.out.println(fwdIndexVal2 * NOTIONAL);
  }

  @Test
  void optionPriceTest() {
    final double expFwdSpread1 = 333.22161805754155 * ONE_BP;
    final double[] expPayer = new double[] {1166.52326219765, 7845.27322803508, 40558.1282159139, 158856.949120083, 536257.749918445, 1058919.34502316, 1896693.60414217, 2859531.98986528,
      3855170.04257168 };
    final double[] expRec = new double[] {4144974.90985036, 3151806.88024909, 2184672.95566985, 1303124.99700691, 536257.749918445, 203493.833775765, 41421.3133276675, 4412.91948366604,
      204.192622958409 };
    final double[] expVol = new double[] {0.296997815784953, 0.296882160844549, 0.296755324292371, 0.296615666451983, 0.29643812343842, 0.296288172819755, 0.29609430175152, 0.295875936515983,
      0.295628070098743 };

    final double tE = ACT365F.getDayCountFraction(TRADE_DATE, EXPIRY);
    final double tES = ACT365F.getDayCountFraction(TRADE_DATE, EXERCISE_SETTLE);

    final IndexOptionPricer oPricer = new IndexOptionPricer(FWD_CDX, tE, YIELD_CURVE, INDEX_COUPON);
    //build credit curve assuming par spreads
    final ISDACompliantCreditCurve cc = CREDIT_CURVE_BUILDER.calibrateCreditCurve(PILLAR_CDX, PILLAR_PUF, YIELD_CURVE);

    final double q = cc.getSurvivalProbability(tE);
    final double df = YIELD_CURVE.getDiscountFactor(tES);

    final double fwdProt = PRICER.protectionLeg(FWD_START_CDX, YIELD_CURVE, cc, 0) + df * (1 - INDEX_RECOVERY) * (1 - q);
    final double fwdAnn = PRICER.annuity(FWD_START_CDX, YIELD_CURVE, cc, PriceType.CLEAN, 0);
    final double fwdSpread = fwdProt / fwdAnn;

    assertEquals("Regression test for index curve ATM Fwd spread", expFwdSpread1, fwdSpread, 1e-15);
    final double fwdIndexVal = ((fwdProt - INDEX_COUPON * fwdAnn) / df);

    final double vol = 0.3;
    final double x0 = oPricer.calibrateX0(fwdIndexVal, vol);

    if (PRINT) {
      System.out.println("df: " + df);
      System.out.println("X0: " + x0 * TEN_THOUSAND);
    }

    final int n = STRIKES.length;
    for (int i = 0; i < n; i++) {
      final double g = 1 - STRIKES[i] * ONE_PC;
      final double payer1 = oPricer.getOptionPriceForPriceQuotedIndex(1 - BBG_ATM_FWD, vol, g, true);
      final double recevier1 = oPricer.getOptionPriceForPriceQuotedIndex(1 - BBG_ATM_FWD, vol, g, false);
      final double payer2 = oPricer.getOptionPriceForPriceQuotedIndex(fwdIndexVal, vol, g, true);
      final double recevier2 = oPricer.getOptionPriceForPriceQuotedIndex(fwdIndexVal, vol, g, false);

      double vol1 = 0;
      double vol2 = 0;
      if (1 - BBG_ATM_FWD < g) {
        vol1 = oPricer.impliedVol(1 - BBG_ATM_FWD, g, CALLPRICE[i] / NOTIONAL, true);
        vol2 = oPricer.impliedVol(fwdIndexVal, g, CALLPRICE[i] / NOTIONAL, true);
      } else {
        vol1 = oPricer.impliedVol(1 - BBG_ATM_FWD, g, PUTPRICE[i] / NOTIONAL, false);
        vol2 = oPricer.impliedVol(fwdIndexVal, g, PUTPRICE[i] / NOTIONAL, false);
      }
      if (PRINT) {
        System.out.println(STRIKES[i] + "\t" + payer1 * NOTIONAL + "\t" + recevier1 * NOTIONAL + "\t" + payer2 * NOTIONAL + "\t" + recevier2 * NOTIONAL + "\t" + vol1 + "\t" + vol2);
      }
      assertEquals("Regression test for payer option", expPayer[i] / NOTIONAL, payer1, 1e-15);
      assertEquals("Regression test for reciver option", expRec[i] / NOTIONAL, recevier1, 1e-15);
      assertEquals("Regression test for implied vol", expVol[i], vol1, 1e-15);
    }
  }
}
